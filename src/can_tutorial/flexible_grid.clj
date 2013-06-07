(ns can-tutorial.flexible-grid
  (:use quil.core))

;; Global variables declaration
;; window size
(def WIDTH 640)
(def HEIGHT 480)
;; Min/max number of columns & rows
(def COLUMNS [2 30])
(def ROWS [2 50])

;; A color list defined as a vector of vectors
;; it contains 5 RGB color combinations
;; Colors courtesy of:
;; http://www.colourlovers.com/palette/2799878/Kandi_Floss
(def colors [[255 0 114] [255 97 168] [184 247 255] [20 229 255] [0 185 246]])

(defn compute-grid
  "Takes an x/y position, window width/height and min/max ranges
  for columns & rows, returns a map with these keys:

      :cols, :rows - number of columns/rows
      :cell-w, :cell-h - width/height of single grid cell

  Uses quil's `map-range` fn (the equivalent to Processing's map
  function) to vary/compute the number of cols/rows for given position:

      (map-range value low1 high1 low2 high2)"
  [x y width height cols rows]
  (let [cols (apply map-range x 0 width cols)
        rows (apply map-range y 0 height rows)
        cell-w (/ width cols)
        cell-h (/ height rows)]
    {:cols cols :rows rows :cell-w cell-w :cell-h cell-h}))

(defn draw-colored-cell
  "Draws a colored rect at given position and size. Applies the RGB values
  of col as arguments to Quil's fill function."
  [x y w h col]
  (apply fill col)
  (rect x y w h))

(defn setup
  []
  (smooth)
  (frame-rate 15))

(defn draw
  []
  (background 0)
  (let [grid (compute-grid (mouse-x) (mouse-y) (width) (height) COLUMNS ROWS)
        ;; destructure returned grid map
        {:keys [cols rows cell-w cell-h]} grid]
    ;; Next we're using `for` to draw a number of rectangles.
    ;; Technically however, `for` is constructing a lazy seq of
    ;; values, but drawing doesn't produce any actual values. In
    ;; computational terms it's just a side effect.
    ;; Therefore we need to wrap `for` with `dorun`, which forces
    ;; the evaluation of the entire lazyseq produced by `for` and
    ;; with it also executes our side effects (the drawing of rects).
    ;; For more information:
    ;; - http://onclojure.com/2009/03/04/dorun-doseq-doall/
    (dorun
     ;; Nested for loop:
     ;; iteration over x is the outer loop
     ;; iteration over y is the inner loop
     (for [x (range cols)
           y (range rows)
           ;; Creates local bindings to calculate position
           ;; for each iteration/grid cell
           :let [pos-x (* x cell-w)
                 pos-y (* y cell-h)]]
       ;; draw single grid cell with a random color from the colors vector
       (draw-colored-cell pos-x pos-y cell-w cell-h (rand-nth colors))))))

(defsketch flexible-grid
  :title "Flexible Grid Example"
  :draw draw
  :setup setup
  :size [WIDTH HEIGHT])
