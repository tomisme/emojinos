(ns emojinos.game)

;; position = [x y]
;; tile = [emoji x y]
;; board = #{} of tiles
;; rule = [left right]
;; path = {} of :direction, :tiles, :walking?
;; effect = {} of tiles => emojis

(def all-directions
  [[0 1]
   [1 0]
   [0 -1]
   [-1 0]])

(defn positive-direction? [direction]
  (or (= [0 1] direction)
      (= [1 0] direction)))

(defn pos-neighbors [x y]
  #{[(inc x) y]
    [x (inc y)]
    [(dec x) y]
    [x (dec y)]})

(defn filled-positions [board]
  (into #{}
        (map (fn [[_ x y]]
               [x y])
             board)))

(defn targets [board]
  (let [filled (filled-positions board)]
    (reduce
     (fn [coll [_ x y]]
       (loop [to-check (seq (pos-neighbors x y))
              empty-neighbors #{}]
           (if (empty? to-check)
             (into coll empty-neighbors)
             (let [checking (first to-check)]
               (recur (rest to-check)
                      (if (contains? filled checking)
                        empty-neighbors
                        (conj empty-neighbors checking)))))))
     #{}
     board)))

(defn get-tile-at [x y board] ;; => nil || tile
  (some (fn [tile]
          (let [[_ x2 y2] tile]
            (when (and (= x x2)
                       (= y y2))
              tile)))
        board))

(defn get-neighbors [start-x start-y board]
  (into #{}
        (filter boolean
                (map (fn [[x y]]
                       (get-tile-at x y board))
                     (pos-neighbors start-x start-y)))))

(defn root? [rule tile]
  (let [[[root]] rule
        [emoji] tile]
    (= root emoji)))

(defn walk-paths [board rule-left paths step] ;; => #{} of paths
  (into #{}
        (map (fn [{:keys [direction tiles walking?] :as path}]
               (if (not walking?)
                 (assoc path :walking? false)
                 (let [[_ x y] (last tiles)
                       [dx dy] direction
                       next-tile (get-tile-at (+ x dx) (+ y dy) board)]
                   (if (and next-tile
                            (= (get next-tile 0)
                               (get rule-left step)))
                     (update path :tiles conj next-tile)
                     (assoc path :walking? false)))))
             paths)))

(defn paths->effects [paths [rule-left rule-right]] ;; => nil || #{} of effects
  (let [len (count rule-left)
        ->tiles (fn [tiles emojis]
                  (map-indexed (fn [idx tile]
                                 (nth emojis idx))
                               tiles))]
    (reduce (fn [effects {:keys [tiles direction]}]
              (if (< (count tiles) len)
                effects
                (let [effect (zipmap tiles
                                     (if (positive-direction? direction)
                                       (->tiles tiles rule-right)
                                       (->tiles tiles (reverse rule-right))))]
                  (if effects
                    (conj effects effect)
                    (conj #{} effect)))))
            nil
            paths)))

;; TODO shouldn't return an empty set (e.g. when paths are walked to no effect)
(defn get-tile-effects [board rule tile] ;; => nil || #{} of effects
  (when (root? rule tile)
    (let [[root-emoji root-x root-y] tile
          [rule-left rule-right] rule
          total-steps (count rule-left)]
      (loop [step 0
             paths (into #{}
                         (map (fn [direction]
                                {:direction direction
                                 :tiles [tile]
                                 :walking? true})
                              all-directions))]
        (if (= step total-steps)
          (paths->effects paths rule)
          (recur (inc step)
                 (walk-paths board rule-left paths (inc step))))))))

(defn get-rule-effects [board rule] ;; => nil || #{} of effects
  (reduce (fn [rule-effects tile]
            (if-let [effects (get-tile-effects board rule tile)]
              (if rule-effects
                (conj rule-effects effects)
                (clojure.set/union #{} effects))
              rule-effects))
          nil
          board))

(defn get-first-tripped-rule-effects [board rules] ;; nil || #{} of effects
  (some (fn [rule]
          (get-rule-effects board rule))
        rules))

(defn apply-effect [starting-board effect]
  (reduce (fn [board [tile emoji]]
            (if emoji
              (-> board
                  (disj tile)
                  (conj [emoji (nth tile 1) (nth tile 2)]))
              (disj board tile)))
          starting-board
          effect))

(defn apply-effects [board effects]
  (reduce apply-effect board effects))

(defn build-effects-chain [starting-board rules]
  (loop [board starting-board
         chain []]
    (let [effects (get-first-tripped-rule-effects board rules)]
      (if (empty? effects)
        chain
        (recur (apply-effects board effects)
               (conj chain effects))))))

(defn resolve-board [board rules]
  (let [effects-chain (build-effects-chain board rules)]
    (reduce apply-effects board effects-chain)))
