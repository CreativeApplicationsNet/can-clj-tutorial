(ns can-tutorial.wordcount
  (:use quil.core))

;; path to text file to analyze
(def src-path (atom "README.md"))

;; max number of words to display (top 100)
(def max-words 100)

;; words to ignore in histogram
(def ignore-words
  #{"the" "a" "an" "and" "be" "is" "are" "in" "of" "to"})

;; container for histogram/word stats
(def histogram (atom nil))

(def start-time (atom 0))

(defn word-stats
  "Takes a file path, reads the file contents, forms a sequence of words,
  makes them all lowercase, computes word frequencies and returns a sorted
  vector of [word count] items, with the most frequent words at the beginning."
  [path]
  (->> (slurp path)
       (re-seq #"[A-Za-z']+")
       (map #(.toLowerCase %))
       (frequencies)
       (sort-by second)
       (reverse)))

(defn exclude-items
  "Takes a nested vector of word stats as returned by `word-stats` fn and a
  set of words to exclude. Returns filtered stats without those words."
  [stats items]
  (filter (fn [[word _]] (not (items word))) stats))

(defn tween-ease-out
  "Computes a tween value based on the given index i, the current time t since
  the beginning of the animation, a delay value for each consecutive index i and
  an overall tween duration. The result will be clipped between 0.0 ... 1.0 and
  is being mapped on the 1st quadrant of the sine fn to create an ease-out effect."
  [i t delay duration]
  (-> (- delay) (* i) (+ t) (/ duration) (min 1.0) (max 0.0) (* HALF-PI) (sin)))

(defn draw-word
  "Draws a bar & label for a single word. Takes a single histogram entry,
  an index, bar width and normalization factor, computes the position
  and size of the bar, then draws it. Assumes Quil's color-mode is set to
  normalized HSB range."
  [[word freq] idx bar-width anim norm]
  (let [;; compute color hue from word's frequency
        hue (* freq norm)
        ;; compute bar height
        bar-height (* freq anim norm (- (height) 175))
        ;; top/left bar position on screen
        x (+ 25 (* idx bar-width))
        y (- (height) 150 bar-height)]
    ;; set HSB fill color & draw bar
    (fill hue 1.0 anim)
    (rect x y (dec bar-width) bar-height)
    ;; save coordinate system
    (push-matrix)
    ;; move to bottom left corner of bar
    (translate x (- (height) 150))
    ;; rotate 90 degrees CCW
    (rotate (* -0.5 PI))
    ;; set HSB white
    (fill 0 0 anim)
    ;; label = "word (count w/ leading zeros)"
    ;; same as Java's String.format() or C's sprintf()
    (text (format "%s (%02d)" word freq) -10 (* bar-width 0.75))
    ;; restore original coordinate system
    (pop-matrix)))

(defn draw
  "Main draw function for Quil/Processing. Only processes the top `max-words`
  stats from the histogram and displays them as bar chart."
  []
  (let [words (take max-words @histogram)
        ;; count words explicitly, since we might not have 100 uniques
        num-words (count words)
        ;; compute width of single bar (leaving some border on left/right)
        bar-width (/ (- (width) 50) num-words)
        ;; extract the word count of most popular (first) word in histogram
        [_ max-freq] (first words)
        ;; normalize
        norm-factor (/ 1.0 max-freq)
        now (- (System/currentTimeMillis) @start-time)]
    (background 0)
    (no-stroke)
    ;; use HSB color mode in the range 0.0 ... 1.0
    (color-mode :hsb 1.0)
    ;; draw all labels right aligned
    (text-align :right)
    ;; draw all words/bars, w/ applied anim scale factor
    ;; bars fade in over first 1500ms (w/ 30ms extra delay per consecutive bar)
    (doseq [[i w] (zipmap (range) words)]
      (draw-word w i bar-width (tween-ease-out i now 30 1500) norm-factor))
    ;; draw global stats
    (text "unique words:\nmax word freq:" (- (width) 75) 37)
    (text (format "%04d\n%d" (count @histogram) max-freq) (- (width) 25) 37)))

(defn setup
  "Initialization function for our sketch.
  Initializes the histogram with the result of calling word-stats with the current src-path.
  Also resets the start-time atom to current epoch."
  []
  (reset! histogram (-> @src-path (word-stats) (exclude-items ignore-words)))
  (reset! start-time (System/currentTimeMillis)))

(defsketch wordcount
  :title "Word histogram"
  :size [1280 720]
  :setup setup
  :draw draw)

(add-watch src-path :update-stats (fn [& _] (setup)))
