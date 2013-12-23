(ns fluids.demo1
  (:require [kachel.core :as grid]
            [clojure.edn :as edn])
  (:import [javax.swing JFrame JPanel SpringLayout]
           [java.awt Color Dimension]
           [kachel.core SquareGrid]))

(defn paint-background [g width height]
  (.setColor g Color/BLACK)
  (.fillRect g 0 0 width height))

(defn paint-grid [g size width height]
  (.setColor g Color/GRAY)
  (doseq [x (map (partial * size) (range (inc width)))]
    (.drawLine g x 0 x (* height size)))
  (doseq [y (map (partial * size) (range (inc height)))]
    (.drawLine g 0 y (* width size) y)))

(defmulti render-cell (fn [_ _ _ _ cell] (:type cell)))

(defmethod render-cell :air [g x y size cell])

(defmethod render-cell :terrain [g x y size cell]
  (doto g
    (.setColor (Color. 0 128 0))
    (.fillRect (* size x)
               (* size y)
               size
               size)))

(defn render [g sim width height]
  (let [grid-width (.width @(sim :world))
        grid-height (.height @(sim :world))
        cell-size (sim :cell-size)]
    (paint-background g width height)
    (doseq [y (range grid-height)
            x (range grid-width)]
      (render-cell g x y cell-size @(grid/coordinate->field @(sim :world) [x y])))
    (paint-grid g cell-size grid-width grid-height)))

(defn run [& args]
  (let [world (grid/square-grid :width 40
                                :height 30
                                :default-fn (fn [& _]
                                              (atom {:type :air})))
        _ (doseq [x (range 40)
                  y (range 15 30)]
            (reset! (grid/coordinate->field world [x y]) {:type :terrain}))
        _ (doseq [y (range 15 25)
                  x [10 15 20 25]]
            (reset! (grid/coordinate->field world [x y]) {:type :air}))
        _ (doseq [x (concat (vec (range 10 16))
                            (vec (range 20 26)))]
            (reset! (grid/coordinate->field world [x 25]) {:type :air}))
        _ (doseq [y (range 15 20)]
            (reset! (grid/coordinate->field world [25 y]) {:type :terrain}))
        _ (doseq [c [[30 13]
                     [30 12]
                     [26 14]
                     [26 13]
                     [26 11]
                     [25 11]
                     [25 13]
                     [24 11]
                     [24 13]
                     [23 11]
                     [23 12]
                     [23 13]
                     [26 10]
                     [33 14]
                     [33 13]
                     [33 12]
                     [33 11]
                     [35 13]
                     [35 12]
                     [36 12]
                     [37 12]
                     [39 14]
                     [39 13]
                     [39 12]
                     [39 11]
                     [39 10]]]
            (reset! (grid/coordinate->field world c) {:type :terrain}))
        _ (comment (spit "demo.edn" (pr-str {:fields (map deref (.fields world))
                                             :width (.width world)
                                             :height (.height world)})))
        data (read-string (slurp "demo.edn"))
_ (prn data)
        world (SquareGrid. (:width data)
                           (:height data)
                           (mapv ref (:fields data))
                           false false)
        simulation {:world (ref world)
                    :cell-size 16}
        panel (proxy [JPanel] []
                (paintComponent [g]
                  (proxy-super paintComponent g)
                  (render g simulation (.getWidth this) (.getHeight this))))
        frame (JFrame. "Liquid test")
        layout (SpringLayout.)
        content-pane (.getContentPane frame)]
    (doto panel
      (.setPreferredSize (Dimension. 800 500)))
    (doto content-pane
      (.setLayout layout)
      (.add panel))
    (doto layout
      (.putConstraint SpringLayout/WEST panel 5 SpringLayout/WEST content-pane)
      (.putConstraint SpringLayout/NORTH panel 5 SpringLayout/NORTH content-pane)
      (.putConstraint SpringLayout/EAST content-pane 5 SpringLayout/EAST panel)
      (.putConstraint SpringLayout/SOUTH content-pane 5 SpringLayout/SOUTH panel))
    (doto frame
      .pack
      (.setVisible true))))
