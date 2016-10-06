;;
;; See  seciton  "Custom  Layer  Example"  of  the  Leaflet  reference
;; [1]. Note that the example in the docs recommends extending L.Class
;; which worked  with Leaflet 0.7  but stopped working with  1.0. Note
;; use of L.Layer.extend below.
;;
;; [1] http://leafletjs.com/reference.html
;;
(ns leaves.layer)

;; Update xy-coordinates of layer points in one transaction applied to
;; a reagent atom holding  geographic coordinates.  Note that dragging
;; the map does not (need to)  trigger coordinate update --- the whole
;; layer  with the  nested elements  is  translated as  a whole.   But
;; zooming in  does change  the xy  coordinates. You  may think  of xy
;; coordinates as a  pure function of lattitude,  longitude *and* zoom
;; level.
(defn- update-xy
  [points ll->xy]
  (let [tx (fn [pts]
             (let [ll (:ll pts)
                   xy (for [p ll]
                        (ll->xy p))]
               (println {:ll ll :xy xy})
               (assoc pts :xy xy)))]
    (swap! points tx)))

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
                                          "leaflet-zoom-hide")
                  ;; We will  need a function to  transform geographic
                  ;; coordinates into plain pixels.  The input and the
                  ;; output are cljs 2-vectors here:
                  ll->xy (fn [p]
                           (let [ll (clj->js p)
                                 xy (.latLngToLayerPoint map ll)]
                             [(.-x xy) (.-y xy)]))]
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
              (let [points (.-x-points this)]
                (doto map
                  (.on "viewreset" #(update-xy points ll->xy))
                  (.on "zoomend" #(update-xy points ll->xy)))
                (update-xy points ll->xy)))))

        :onRemove
        (fn [map]
          ;; remove layer's DOM elements and listeners
          (this-as this
            (-> map
                .getPanes
                .overlayPane
                (.removeChild (.-x-el this)))
            ;; Remove all event  listeners. To be specific  we need to
            ;; keep a reference to the callbacks we added in onAdd and
            ;; pass them here again:
            (doto map
              (.off "viewreset")
              (.off "zoomend"))))}))

;; (println {:my-custom-layer MyCustomLayer})


