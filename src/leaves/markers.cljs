(ns leaves.markers)

;; This may be browser dependent:
(defn transform [x y]
  (str "translate3d(" x "px," y "px, 0px)"))

(defn svg-marker [d color]
  (let [r (/ d 2)]
    [:div {:style {:margin 0
                   :transform (transform (- r) (- r))}}
     [:svg {:width d, :height d
            :style {:background-color "#fff0"}}
      [:circle {:cx r, :cy r, :r r, :style {:fill color}}]]]))

;; See SVG docs for the shadow effect using svg filters [1].
;; [1] http://www.w3schools.com/graphics/svg_feoffset.asp
(defn svg-marker-with-shadow [d color]
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


(defn svg-leaf [size color]
  [:div {:style {:transform (transform (* -0.5 size) (- size))}}
   [:svg {:width size :height size
          :viewBox "0 0 430.114 430.114"}
    [:path {:fill color
            :d "M356.208,107.051c-1.531-5.738-4.64-11.852-6.94-17.205C321.746,23.704,261.611,0,213.055,0
C148.054,0,76.463,43.586,66.905,133.427v18.355c0,0.766,0.264,7.647,0.639,11.089c5.358,42.816,39.143,88.32,64.375,131.136
c27.146,45.873,55.314,90.999,83.221,136.106c17.208-29.436,34.354-59.259,51.17-87.933c4.583-8.415,9.903-16.825,14.491-24.857
c3.058-5.348,8.9-10.696,11.569-15.672c27.145-49.699,70.838-99.782,70.838-149.104v-20.262
C363.209,126.938,356.581,108.204,356.208,107.051z M214.245,199.193c-19.107,0-40.021-9.554-50.344-35.939
c-1.538-4.2-1.414-12.617-1.414-13.388v-11.852c0-33.636,28.56-48.932,53.406-48.932c30.588,0,54.245,24.472,54.245,55.06
C270.138,174.729,244.833,199.193,214.245,199.193z"}]]])

(defn popup-marker [text]
  [:div.leaflet-popup {:style {:display nil}}
   [:div.leaflet-popup-content-wrapper {:style {:border-radius "4px"}}
    [:div.leaflet-popup-content {:style {:margin "4px"}}
     text]]
   #_[:div.leaflet-popup-tip-container
      [:div.leaflet-popup-tip]]])

(defn png-marker []
  ;; There is something important about in the CSS for
  ;; "leaflet-marker-icon" which makes positioning work also for
  ;; many icons. Do not omit it util it is figured out:
  [:img {:src "img/marker-icon.png"
         :class "leaflet-marker-icon"
         :style {:margin-left "-12px"
                 :margin-top "-41px"
                 :width "25px"
                 :height "41px"}}])

(defn translated [x y body]
  [:div {:style {:transform (transform x y)}}
   body])

