(ns hello-world.core
  (:require [reagent.core :as r]))

;; Enable output of println and co to the js console:
(enable-console-print!)

(println "This text is printed from src/hello-world/core.cljs. Go
ahead and edit it and see reloading in action.")

(js/console.log "Log message")

;; Define your app data so that it doesn't get over-written on reload
(defonce app-state (r/atom {:text "Hello world!"}))

(defn some-component []
  [:div
   [:h3 "I am component 1!"]
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
       [:h3 "I am component 2!"]
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

(defn app []
  [:div
   [:h2 "I am supercomponent!"]
   [some-component]
   [timer-component]
   [stateful-component]])

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

(r/render-component [app]
                    (.-body js/document))
