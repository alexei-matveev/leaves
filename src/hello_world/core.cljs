(ns hello-world.core
  (:require [reagent.core :as r]
            [cljsjs.react-bootstrap :as b]))

;; Enable output of println and co to the js console:
(enable-console-print!)

(println "This text is printed from src/hello-world/core.cljs. Go
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
      ;; component:
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div
       [:h3 "I am component number two!"]
       [:p.someclass  "Seconds Elapsed: " @seconds-elapsed]])))

(defn stateful-component []
  [:div
   [:h3 "I am a stateful component!"]
   [:p.someclass
    {:on-click #(swap! app-state
                       (fn [s]
                         (let [txt (:text s)]
                           (assoc s :text (str txt " Hello?")))))}
    (:text @app-state)]])

;;
;; Leaflet component:
;;
(defn leaflet-render []
  [:div#map {:style {:height "360px"}}])

(defn leaflet-did-mount []
  (let [tiles "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution "Map data © <a href=\"http://openstreetmap.org\">OpenStreetMap</a> contributors"
        map (.setView (.map js/L "map") #js [51.505 -0.09] 13)]
    (.addTo (.tileLayer js/L tiles #js {:attribution attribution
                                        :maxZoom 18})
            map)
    (println "adding custom layer")
    (.addLayer map (js/MyCustomLayer. #js [51.505 -0.09]))))

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

(r/render-component
 [timer-component]
 (js/document.getElementById "my-layer-id"))
