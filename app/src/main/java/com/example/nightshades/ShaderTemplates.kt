package com.example.nightshades

object ShaderTemplates {
    const val CANVAS_ITEM = """
        shader_type canvas_item;
        void fragment() {
            COLOR = texture(TEXTURE, UV);
        }
    """

    const val SPATIAL = """
        shader_type spatial;
        void vertex() {
            // Modifie la position des sommets ici
        }
        void fragment() {
            ALBEDO = vec3(0.5);
        }
    """

    const val PARTICLES = """
        shader_type particles;
        void start() {
            // Initialisation
        }
        void process() {
            // Animation
        }
    """

    const val RADIAL_BLUR = """
        shader_type canvas_item;
        uniform sampler2D screen_texture : hint_screen_texture, filter_linear_mipmap;
        uniform float blur_power : hint_range(0.0, 1.0) = 0.01;      
        uniform float chromatic_power : hint_range(0.0, 1.0) = 0.02; 
        uniform vec2 center = vec2(0.5, 0.5);                       
        uniform int samples = 20; 
        void fragment() {
            vec2 direction = SCREEN_UV - center;
            float dist = length(direction);
            float factor = dist * blur_power;
            vec3 final_color = vec3(0.0);
            for(int i = 0; i < samples; i++) {
                float scale = 1.0 - factor * (float(i) / float(samples - 1));
                vec2 uv_r = center + direction * scale * (1.0 + chromatic_power * dist);
                vec2 uv_g = center + direction * scale;
                vec2 uv_b = center + direction * scale * (1.0 - chromatic_power * dist);
                final_color += vec3(texture(screen_texture, uv_r).r, texture(screen_texture, uv_g).g, texture(screen_texture, uv_b).b);
            }
            COLOR = vec4(final_color / float(samples), 1.0);
        }
    """
}