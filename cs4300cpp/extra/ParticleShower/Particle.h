#ifndef _PARTICLE_H
#define _PARTICLE_H

#include <glm/glm.hpp>

class Particle
{
public:
  Particle(glm::vec4 pos,glm::vec4 vel,glm::vec4 col,float mass,float startTime)
    :position(pos)
    ,velocity(vel)
    ,color(col)
    ,mass(mass)
    ,startTime(startTime)
  {

  }
  ~Particle() {}
  inline float getStartTime() { return startTime;}
  inline float getMass() { return mass;}
  inline glm::vec4 getPosition() {return position;}
  inline glm::vec4 getColor() {return color;}
  inline glm::vec4 getVelocity() {return velocity;}
private:
  const glm::vec4 position;
  const glm::vec4 velocity;
  const glm::vec4 color;

  const float mass;
  const float startTime;



};

#endif // _PARTICLE_H
