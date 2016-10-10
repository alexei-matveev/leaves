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

