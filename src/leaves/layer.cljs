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
          ;; Constructor.  Save the  atom holding  coordinates of  the
          ;; layer points.  We wil need  to update them on some events
          ;; later.
          (this-as this
            (println {:state @points})
            (set! (.-x-points this) points)))

        :onAdd
        (fn [map]
          (this-as this
            ;; Create a DOM element and put it into one of the map
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
              ;; The id is referred to  in the react component. FIXME:
              ;; literal is a bad idea:
              (set! (.-id el) "my-layer-id")
              (js/console.log "MyCustomLayer: element created")
              ;; It probably need  to be nested here in  order to move
              ;; together with the tiles when dragging the map around:
              (-> map
                  .getPanes
                  .-overlayPane
                  (.appendChild el))
              ;; Add  a viewreset  event listener  for updating  pixel
              ;; coordinates of layer points and do that for the first
              ;; time too:
              (let [points (.-x-points this)
                    callback #(update-xy points ll->xy)]
                (doto map
                  (.on "viewreset" callback)
                  (.on "zoomend" callback))
                (callback)))))

        :onRemove
        (fn [map]
          ;; Remove layer's DOM elements and listeners
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


