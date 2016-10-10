(ns leaves.core
  (:require [reagent.core :as r]
            [leaves.layer :as layer]
            [leaves.markers :as m]
            [leaves.cities :as cities]))

;; Enable output of println and co to the js console:
(enable-console-print!)

(println "This text is printed from src/leaves/core.cljs. Go
ahead and edit it and see reloading in action.")

;; Both seem to be used in the wild:
(js/console.log "Log message one way")
(.log js/console "Log message another way")

;; Define your app data so that it doesn't get over-written on reload
(defonce app-state (r/atom {:text "Hello world!"}))

(defn some-component []
  [:div
   [:h3 "I am component number one!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "blue"}} " and blue"]
    " text."]])

(defn timer-component []
  ;; This state atom is reset on every reload, also by figwheel:
  (let [seconds-elapsed (r/atom 0)]
    ;; A component does not need to return plain hiccup data. The
    ;; component is a function itself and may also return another
    ;; function that returns such data:
    (fn []
      ;; A change of my local atom will trigger re-rendering of this
      ;; component. FIXME: and install another ticker!
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div>p
       {:style {:margin 0}
        :on-click #(swap! seconds-elapsed (constantly -1))}
       "Timer-" @seconds-elapsed])))

(defn stateful-component []
  [:div
   [:h3 "I am a stateful component!"]
   [:p.someclass
    {:on-click #(swap! app-state
                       (fn [s]
                         (let [txt (:text s)]
                           (assoc s :text (str txt " Hello?")))))}
    (:text @app-state)]])

(def points (r/atom (for [[n lon lat] cities/cities]
                      {:name n
                       :ll [lat lon]
                       :xy nil
                       :flag (> (rand 1) 0.99)})))

;; Flip some flags with some low probability:
(defn- update-flags [pts]
  (for [p pts]
    (let [flag (:flag p)]
      ;; Red flag (true) will flip with high probabiliy back to
      ;; green. The green flag will flip to read with low probability:
      (if (or (and (not flag)           ; green
                   (< (rand 1) 0.01))
              (and flag                 ; red
                   (< (rand 1) 0.1)))
        (update-in p [:flag] not)
        p))))

#_(println @points)

(defn- leaves []
  (let [pts @points
        xy (map :xy pts)
        ;; We will be replicating the same object in the hope to get
        ;; some caching down the call chain:
        red [:div [m/svg-marker-with-shadow 16 "red"]]
        green [:div [m/svg-marker-with-shadow 16 "green"]]]
    (do
      ;; FIXME: When re-rendered too often for reasons other than some
      ;; falgs being updated, the rate may rise:
      (js/setTimeout #(swap! points update-flags) 1000)
      [:div
       (for [[i p] (map-indexed vector pts)]
         (let [[x y] (:xy p)
               flag (:flag p)
               name (:name p)
               marker (if flag
                        [:div red [m/popup-marker name]]
                        green)]
           ;; Meta with the key for react.js to tell the elements apart.
           ;; Note that annotating the marker with metadata in prefix
           ;; form does not seem to suffice:
           (with-meta
             (m/translated x y marker)
             {:key i})))])))

;;
;; Leaflet component with handlers:
;;
(defn leaflet []
  (let [tile-url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution "Map © <a href=\"http://openstreetmap.org\">OSM</a>"
        ;; This is Munich, London would be [51.505 -0.09]
        center #js [48.1351 11.5820]
        zoom 5]
    (r/create-class
     {:reagent-render
      (fn []
        [:div#map-id {:style {:height "800px"}}])
      :component-did-mount
      (fn []
        (let [tile-layer (js/L.tileLayer tile-url
                                         #js {:attribution attribution
                                              :maxZoom 18})
              options #js {:center center
                           :zoom zoom
                           ;; The custom layer does not handle zoom
                           ;; animation yet. Disable altogether:
                           :zoomAnimation false}
              map (js/L.map "map-id" options)
              ;; Custom layer takes an atom where plane cooridnates
              ;; will be updated occasionally:
              custom-layer (layer/MyCustomLayer. points)]
          (doto map
            (.addLayer tile-layer)
            (.addLayer custom-layer))))})))

(defn app []
  [:div
   [:h2 "I am a supercomponent!"]
   [some-component]
   [timer-component]
   [stateful-component]])

;; Optionally touch your app-state to force rerendering depending on
;; your application
#_(defn on-js-reload
  []
  (swap! app-state update-in [:__figwheel_counter] inc))

;; React would warn about using of (.-body js/document) here, and
;; advise using specific element:
(r/render-component
 [leaflet]
 (js/document.getElementById "app"))

;; The element id is set in the constructor of the custom layer:
(r/render-component
 [leaves]
 (js/document.getElementById "my-layer-id"))

;; (println layer/MyCustomLayer)

