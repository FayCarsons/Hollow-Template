(ns template.core
  (:require [hollow.util :as u]
            [hollow.webgl.core :refer [start-hollow!
                                       hollow-context]]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-purefrag-shader!]]
            [kudzu.tools :refer [unquotable]]
            [kudzu.core :refer [kudzu->glsl]]))

(def render-frag
  (unquotable
   (kudzu->glsl
    '{:precision {float highp}
      :outputs {fragColor vec4}
      :uniforms {size vec2}
      :main ((= fragColor (vec4 (/ gl_FragCoord.xy
                                   size)
                                0
                                1)))})))

(defn draw-canvas! [gl]
  (maximize-gl-canvas gl)
  (let [resolution (canvas-resolution gl)]
    (run-purefrag-shader! gl
                          render-frag
                          resolution
                          {"size" resolution})
    {}))

(defn init []
  (start-hollow! draw-canvas! nil))

(defn ^:dev/after-load restart! []
  (js/document.body.removeChild (.-canvas (hollow-context)))
  (init))

(defn pre-init []
  (js/window.addEventListener "load" (fn [_] (init))))