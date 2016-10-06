;;
;; See  seciton  "Custom  Layer  Example"  of  the  Leaflet  reference
;; [1]. Note that the example in the docs recommends extending L.Class
;; which worked  with Leaflet 0.7  but stopped working with  1.0. Note
;; use of L.Layer.extend below.
;;
;; [1] http://leafletjs.com/reference.html
;;
(ns leaves.layer)

;; This is an ugly way to extend a js class:
(def MyCustomLayer
  (js/L.Layer.extend
   #js {:initialize
        (fn [points]
          ;; save position of the layer or any options from the
          ;; constructor
          (this-as this
            (println {:state @points})
            (set! (.-x-points this) points)))

        :onAdd
        (fn [map]
          (this-as this
            (set! (.-x-map this) map)
            ;; create a DOM element and put it into one of the map
            ;; panes
            (let [el (js/L.DomUtil.create "div"
                                          "my-custom-layer leaflet-zoom-hide")]
              (set! (.-x-el this) el)
              ;; The id is referred to in the react component:
              (set! (.-id el) "my-layer-id")
              (js/console.log "MyCustomLayer: element created")
              (-> map
                  .getPanes
                  .-overlayPane
                  (.appendChild el))
              ;; add a viewreset event listener for updating layer's
              ;; position, do the latter
              (-> map
                  (.on "viewreset" (.-x-reset this) this))
              (-> map
                  (.on "zoomend" (.-x-reset this) this))
              (.x-reset this))))

        :onRemove
        (fn [map]
          ;; remove layer's DOM elements and listeners
          (this-as this
            (-> map
                .getPanes
                .overlayPane
                (.removeChild (.-x-el this)))
            (-> map
                (.off "viewreset" (.-x-reset this) this))))

        ;; Clojurescipt converts dashes in the name to underscores:
        :x_reset
        (fn []
          ;; update layer's position
          (this-as this
            (let [points (.-x-points this)
                  ll (:ll @points)
                  f (fn [p]
                      (let [ll (clj->js p)
                            xy (-> this
                                   .-x-map
                                   (.latLngToLayerPoint ll))]
                        [(.-x xy) (.-y xy)]))
                  xy (for [p ll]
                       (f p))]
              (println {:ll ll :xy xy})
              (swap! points #(assoc % :xy xy)))))}))

;; (println {:my-custom-layer MyCustomLayer})


