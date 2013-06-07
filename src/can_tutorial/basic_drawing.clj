(ns can-tutorial.basic-drawing
  (:use quil.core))

(def noise-inc (atom 0))

(defn setup
  []
  (smooth)
  (color-mode :hsb 360 100 100 100)
  (background 0 0 0))

(defn draw
  []
  (swap! noise-inc + 0.035)
  (stroke-weight (* (noise @noise-inc) 5))
  (stroke (mod (frame-count) 360) 100 100)
  (fill 0 0 0 50)
  (let [radius (* (noise @noise-inc) 50)]
    (ellipse (mouse-x) (mouse-y) radius radius)))

(defsketch basic-drawing
  :title "Basic Drawing Example"
  :setup setup
  :draw draw
  :size [640 480])
