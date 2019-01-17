(ns emojinos.ui
  (:require
   [reagent.core]
   [emojinos.ui.frame :refer [ui-component]]))

(defn ^:export main
  []
  (reagent.core/render [ui-component]
                       (.getElementById js/document "app")))
