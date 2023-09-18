(ns town.lilac.date-field.main
  (:require
   ["react-dom/client" :as rdom]
   [helix.core :refer [$]]
   [town.lilac.date-field.app :as app]))


(defonce root (rdom/createRoot (js/document.getElementById "app")))


(defn ^:dev/after-load start!
  []
  (.render root ($ app/main-panel)))
