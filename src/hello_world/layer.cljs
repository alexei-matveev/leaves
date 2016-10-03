(ns hello-world.layer)

(def MyCustomLayer
  (js/L.Class.extend
   #js {:initialize
        (fn [latlng]
          ;; save position of the layer or any options from the
          ;; constructor
          (this-as this
            (set! (.-_latlng this) latlng)
            this))

        :onAdd
        (fn [map]
          (this-as this
            (set! (.-_map this) map)
            ;; create a DOM element and put it into one of the map
            ;; panes
            (let [el (js/L.DomUtil.create "div"
                                          "my-custom-layer leaflet-zoom-hide")]
              (set! (.-_el this) el)
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
                  (.on "viewreset" (.-_reset this) this))
              (._reset this))))

        :onRemove
        (fn [map]
          ;; remove layer's DOM elements and listeners
          (this-as this
            (-> map
                .getPanes
                .overlayPane
                (.removeChild (.-_el this)))
            (-> map
                (.off "viewreset" (.-_reset this) this))))

        :_reset
        (fn []
          ;; update layer's position
          (this-as this
            (let [pos (-> this
                          .-_map
                          (.latLngToLayerPoint (.-_latlng this)))]
              (js/L.DomUtil.setPosition (.-_el this) pos))))}))

;; map.addLayer(new MyCustomLayer(latlng));

(println {:my-custom-layer MyCustomLayer})
