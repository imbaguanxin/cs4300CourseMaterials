#ifndef RAY_H
#define RAY_H

#include <glm/glm.hpp>

/**
 * Created by ashesh on 4/12/2016.
 */
class Ray {
public:
  glm::vec4 start,direction;

  Ray() {
    start = glm::vec4(0.0f,0.0f,0.0f,1);
    direction = glm::vec4(0.0f,0.0f,1.0f,0.0f);
  }
};

#endif // RAY_H
