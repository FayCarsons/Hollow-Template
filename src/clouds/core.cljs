(ns clouds.core
  (:require [hollow.util :as u]
            [hollow.webgl.core :refer [start-hollow!
                                       hollow-context]]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-purefrag-shader!]]
            [hollow.webgl.textures :refer [create-tex]]
            [kudzu.tools :refer [unquotable]]
            [kudzu.core :refer [kudzu->glsl]]))

(def kudzu-wrapper 
  (partial kudzu->glsl
           '{:precision {float highp
                         int highp
                         sampler2D highp
                         usampler2D highp}}))
(def render-frag
  (unquotable
   (kudzu-wrapper
    '{:outputs {fragColor vec4}
      :uniforms {size vec2
                 tex sampler2D}
      :main ((=vec2 pos (/ gl_FragCoord.xy size))
             
             (= fragColor (-> tex
                              .rgb
                              (vec4 1))))})))

(defn render! [{:keys [gl resolution textures] :as state}]
  (run-purefrag-shader! gl
                        render-frag
                        resolution
                        {"size" resolution
                         "tex" (first textures)})
  state)

(defn update-sketch! [{:keys [gl] :as state}]
  (maximize-gl-canvas gl)
  (-> state
      (assoc :resolution (canvas-resolution gl))
      render!))

(defn init-sketch! [gl]
  (let [resolution (canvas-resolution gl)
        textures (u/gen 2 (create-tex gl
                                      :f8
                                      resolution
                                      {:filter-mode :nearest
                                       :wrap-mode :repeat}))]
    {:gl gl
     :textures textures}))

(defn init []
  (start-hollow! init-sketch! update-sketch!))

(defn ^:dev/after-load restart! []
  (js/document.body.removeChild (.-canvas (hollow-context)))
  (init))

(defn pre-init []
  (js/window.addEventListener "load" (fn [_] (init))))