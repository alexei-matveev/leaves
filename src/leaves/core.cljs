(ns leaves.core
  (:require [reagent.core :as r]
            [leaves.layer :as layer]
            [cljsjs.react-bootstrap :as b]))

;; Enable output of println and co to the js console:
(enable-console-print!)

(println "This text is printed from src/leaves/core.cljs. Go
ahead and edit it and see reloading in action.")

;; Both seem to be used in the wild:
(js/console.log "Log message one way")
(.log js/console "Log message another way")

;; Define your app data so that it doesn't get over-written on reload
(defonce app-state (r/atom {:text "Hello world!"}))

(def button (r/adapt-react-class (aget js/ReactBootstrap "Button")))

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

;; This may be browser dependent:
(defn- translate [x y]
  (str "translate3d(" x "px," y "px, 0px)"))

(defn- svg-marker [x y d color]
  (let [r (/ d 2)]
    [:div {:style {:margin 0
                   ;; :margin-top (- r), :margin-left (- r),
                   :transform (translate (- x r) (- y r))}}
     [:svg {:width d
            :height d
            :id "svg-marker"
            :style {:background-color "#fff0"}}
      [:circle {:cx r, :cy r, :r r, :style {:fill color}}]]]))

(defn- svg-component []
  [:div
   (for [x (range -300 300 50)]
     (for [y (range -300 300 50)]
       [svg-marker x y 20 "red"]))])

(def points (r/atom {:ll [[48.1351 11.5820]
                          [48.1451 11.5820]
                          [51.505 -0.09]]
                     :xy nil}))
(println {:atom @points})

(defn- leaves []
  (let [xy (:xy @points)]
    (println {:leaves @points})
    [:div
     (for [[i [x y]] (map-indexed vector xy)]
       ^{:key i} [svg-marker x y 20 "red"])]))
;;
;; Leaflet component handlers:
;;
(defn leaflet-render []
  [:div#map-id {:style {:height "600px"}}])

(defn leaflet-did-mount []
  (let [tile-url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution "Map data © <a href=\"http://openstreetmap.org\">OSM</a> contributors"
        tile-layer (js/L.tileLayer tile-url
                                   #js {:attribution attribution
                                        :maxZoom 18})
        ;; center #js [51.505 -0.09]       ; London
        center #js [48.1351 11.5820]    ; Munich
        map (-> (js/L.map "map-id")
                (.setView center 13))
        ;; Custom layer is so simple so far, that it takes only one
        ;; coordinate pair:
        custom-layer (layer/MyCustomLayer. points)]
    (doto map
      (.addLayer tile-layer)
      (.addLayer custom-layer))))

;;
;; Leaflet component itself:
;;
(defn leaflet []
  (r/create-class {:reagent-render leaflet-render
                   :component-did-mount leaflet-did-mount}))

(defn app []
  [:div
   [:h2 "I am a supercomponent!"]
   [some-component]
   [timer-component]
   [button "with text"]
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

