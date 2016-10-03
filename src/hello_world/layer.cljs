(ns hello-world.layer)

;; This is an ugly way to build a js class:
(def MyCustomLayer
  (js/L.Layer.extend
   #js {:initialize
        (fn [latlng]
          ;; save position of the layer or any options from the
          ;; constructor
          (this-as this
            (set! (.-x-latlng this) latlng)
            this))

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
            (let [pos (-> this
                          .-x-map
                          (.latLngToLayerPoint (.-x-latlng this)))]
              (js/L.DomUtil.setPosition (.-x-el this) pos))))}))

;; map.addLayer(new MyCustomLayer(latlng));

(println {:my-custom-layer MyCustomLayer})
