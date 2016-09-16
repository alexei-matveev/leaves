(ns hello-world.core
  (:require [reagent.core :as r]))

;; Enable output of println and co to the js console:
(enable-console-print!)

(println "This text is printed from src/hello-world/core.cljs. Go
ahead and edit it and see reloading in action.")

(js/console.log "Log message")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn some-component []
  [:div
   [:h3 "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "blue"}} " and blue"]
    " text."]])

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

(r/render-component [some-component]
                    (.-body js/document))
