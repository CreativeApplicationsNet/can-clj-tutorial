(ns can-tutorial.particles
  (:use quil.core))

(def WIDTH 1280)
(def HEIGHT 720)
(def num-particles 10)
(def center-vec {:x (/ WIDTH 2) :y (/ HEIGHT 2)})
;; Particles container atom initialized with an empty list
(def particles-container (atom ()))

;; A color list defined as a vector of vectors
;; it contains 5 RGB color combinations
;; Colors courtesy of:
;; http://www.colourlovers.com/palette/2799689/Craft
(def colors [[51 148 255] [248 0 206] [0 222 222] [106 0 241] [206 255 0]])

;; Our particle factory function
;; We will use this as our template to create particles from
;; The particle itself is simply a map with various keys holding the
;; current state of the particle
(defn particle
  []
  {:position      {:x 0 :y 0}
   :velocity      (random TWO-PI)
   :center-radius 100
   :noise-inc     (random 10000)
   :radius        (+ 0.5 (random 2))
   :fill-color    (rand-nth colors)})

;; Make particles 'n' number of times and returns a lazy sequence of them
(defn make-particles [n]
  (repeatedly n particle))

;; Takes a particle as argument
;; them binds the key values of our particle function to local variables is called destructuring
;; For more information on Destructuring check:
;; - http://blog.jayfields.com/2010/07/clojure-destructuring.html
(defn update-particle
  [{:keys [position velocity center-radius noise-inc] :as particle}]
  (let [x             (+ (:x center-vec) (* (cos velocity) center-radius))
        y             (+ (:y center-vec) (* (sin velocity) center-radius))
        noise-inc     (+ noise-inc 0.025)
        velocity      (- velocity 0.005)
        center-radius (+ center-radius (map-range (noise noise-inc) 0 1 -1 2))]
    ;; assoc[iate] returns a new map, use when changing/updating existing key/value pairs
    (assoc particle
      :noise-inc noise-inc
      :center-radius center-radius
      :velocity velocity
      :position {:x x :y y})))

;; The following takes a collection of particles as argument
;; then passes the collection to the update-particle function and returns another map
(defn update-particles
  [particles]
  (map update-particle particles))

;; Takes a particle position, radius, fill color and draw an ellipse accordingly
(defn draw-particle
  [{:keys [position radius fill-color]}]
  (no-stroke)
  ;; Takes the fill color values and applies them to the fill as arguments
  ;; For more information please refer to:
  ;; - http://clojuredocs.org/clojure_core/clojure.core/apply
  (apply fill fill-color)
  (ellipse (:x position) (:y position) radius radius))

(defn setup
  []
  (background 25)
  (smooth)
  ;; Initialize particles atom
  ;; reset! set the value to a new one regardless of what its currently stored
  (reset! particles-container (make-particles num-particles)))

(defn draw
  []
  ;; Update particles container atom
  (swap! particles-container update-particles)
  ;; Draw each particle by passing it as argument to the draw-particle function
  ;; Find doseq documentation here:
  ;; - http://clojuredocs.org/clojure_core/clojure.core/doseq
  (doseq [particles @particles-container]
    ;; Call draw-particle function and pass particle as argument
    (draw-particle particles)))

(defn mouse-released
  []
  ;; Concat more particles to the existing particles-contaier pool
  ;; Check the use of concat here:
  ;; - http://clojuredocs.org/clojure_core/clojure.core/concat
  (swap! particles-container concat (make-particles num-particles)))

(defsketch particles
  :title "Particles Example"
  :setup setup
  :draw draw
  :mouse-released mouse-released
  :size [WIDTH HEIGHT])
