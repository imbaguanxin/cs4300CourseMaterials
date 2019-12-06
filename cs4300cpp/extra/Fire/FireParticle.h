#ifndef FIREPARTICLE_H
#define FIREPARTICLE_H

#include <glm/glm.hpp>

class FireParticle
{



public :
  FireParticle(const glm::vec3& position, const glm::vec3& dir,
               float size, float temp, float tstart, float lifetime);
  ~FireParticle(){}

  bool isDead();

  bool hasStarted();

  glm::vec3 getPosition();

  float getSize();

  float getTemperature();

  /**
       * This function advances the particle in the simulation. It will assume a new position and a
       * new temperature
       *
       * \param t the absolute time
       */
  void advance(float t);
private:
  //the 3D position of this particle
  glm::vec3 position;
  //its temperature, which will map to its color
  float temperature;
  //the time it was "born"
  float tstart;
  //the size of the particle
  float size;
  //whether the particle has been born
  bool started;
  //whether the particle is dead. In this case it will be resurrected
  bool dead;
  //whether it has started getting smaller
  bool smaller;
  //how long will the particle last
  float lifetime;
  //the direction in which the particle is headed
  glm::vec3 dir;

};

#endif // FIREPARTICLE_H
