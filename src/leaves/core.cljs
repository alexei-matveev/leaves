(ns leaves.core
  (:require [reagent.core :as r]
            [leaves.layer :as layer]
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

;; This may be browser dependent:
(defn- transform [x y]
  (str "translate3d(" x "px," y "px, 0px)"))

(defn- svg-marker [d color]
  (let [r (/ d 2)]
    [:div {:style {:margin 0
                   :transform (transform (- r) (- r))}}
     [:svg {:width d, :height d
            :style {:background-color "#fff0"}}
      [:circle {:cx r, :cy r, :r r, :style {:fill color}}]]]))

;; See SVG docs for the shadow effect using svg filters [1].
;; [1] http://www.w3schools.com/graphics/svg_feoffset.asp
(defn- svg-marker-with-shadow [d color]
  (let [r (/ d 2)
        w (* d 2)]
    [:div {:style {:margin 0
                   :transform (transform (- d) (- d))}}
     [:svg {:width w, :height w}
      [:defs
       [:filter {:id "f1", :x "0", :y "0", :width "200%", :height "200%"}
        [:feOffset {:result "offOut", :in "SourceAlpha", :dx 5, :dy 5}]
        [:feGaussianBlur {:result "blurOut", :in "offOut", :stdDeviation 2}]
        [:feBlend {:in "SourceGraphic", :in2 "blurOut", :mode "normal"}]]]
      [:circle {:cx r, :cy r, :r r, :fill color, :filter "url(#f1)"}]]]))

(defn- popup-marker []
  [:div.leaflet-popup
   [:div.leaflet-popup-content-wrapper
    [:div.leaflet-popup-content
     "Text"]]
   #_[:div.leaflet-popup-tip-container
      [:div.leaflet-popup-tip]]])

(defn- png-marker []
  ;; There is something important about in the CSS for
  ;; "leaflet-marker-icon" which makes positioning work also for
  ;; many icons. Do not omit it util it is figured out:
  [:img {:src "img/marker-icon.png"
         :class "leaflet-marker-icon"
         :style {:margin-left "-12px"
                 :margin-top "-41px"
                 :width "25px"
                 :height "41px"}}])

(defn- translated [x y body]
  [:div {:style {:transform (transform x y)}}
   body])

(def points (r/atom {:ll #_[[48.1351 11.5820]
                          [48.2351 11.5820]]
                     (for [[n lon lat] cities/cities]
                       [lat lon])
                     :xy nil}))
#_(println @points)

(defn- leaves []
  ;; We will be replicating the same object in the hope to get some
  ;; caching down the call chain:
  (let [marker
        [popup-marker]
        #_(png-marker)
        #_[svg-marker-with-shadow 16 "red"]
        xy (:xy @points)]
    [:div
     (for [[i [x y]] (map-indexed vector xy)]
       ;; Meta  with  the  key  for  react.js  to  tell  the  elements
       ;; apart.  Note that  annotating  the marker  with metadata  in
       ;; prefix form does not seem to suffice:
       (with-meta
         (translated x y marker)
         {:key i}))]))

;;
;; Leaflet component handlers:
;;
(defn leaflet-render []
  [:div#map-id {:style {:height "800px"}}])

(defn leaflet-did-mount []
  (let [tile-url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution "Map data © <a href=\"http://openstreetmap.org\">OSM</a> contributors"
        tile-layer (js/L.tileLayer tile-url
                                   #js {:attribution attribution
                                        :maxZoom 18})
        ;; center #js [51.505 -0.09]       ; London
        center #js [48.1351 11.5820]    ; Munich
        options #js {:center center
                     :zoom 6
                     ;; The custom layer does not handle zoom
                     ;; animation yet. Disable altogether:
                     :zoomAnimation false}
        map (js/L.map "map-id" options)
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

