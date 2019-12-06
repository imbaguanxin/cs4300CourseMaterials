#include "FireParticle.h"
#include <cmath>

FireParticle::FireParticle(const glm::vec3& position,
                           const glm::vec3& dir,
                           float size, float temp,
                           float tstart, float lifetime)
{
  this->position = glm::vec3(position);
  this->dir = glm::vec3(dir);
  temperature = temp;
  this->size = size;
  this->tstart = tstart;
  this->lifetime = lifetime;
  dead = false;
  started = false;
  smaller = false;
}

bool FireParticle::isDead()
{
  return dead;
}

bool FireParticle::hasStarted()
{
  return started;
}

glm::vec3 FireParticle::getPosition()
{
  return position;
}

float FireParticle::getSize()
{
  return size;
}

float FireParticle::getTemperature()
{
  return temperature;
}

/**
 * This function advances the particle in the simulation. It will assume a new position and a
 * new temperature
 *
 * \param t the absolute time
 */
void FireParticle::advance(float t)
{
  glm::vec3 disp;

  if (t < tstart) //the particle is not born yet
    return;

  if (t > (tstart + lifetime)) //the particle is already dead
    {
      dead = true;
      return;
    }

  started = true; //start the particle, if not already started

  //the displacement of the particle since it was born.
  disp.x = dir.x * (t - tstart);
  disp.z = dir.z * (t - tstart);
  //the y displacement is a quadratic function. As a result of this, the particle will accelerate upwards as time passes
  disp.y = dir.y + 0.5f * 0.25f * (t - tstart) * (t - tstart);

  //compute the new position
  position = position + disp;

  //now we vary its temperature
  //we can try various temperature functions to see which one gives us the most visually appealing solution

  /*
  Option 1: Temperature is inversely proportional to age, with linear variance
 */
  //temperature = 0.08/(t-tstart);

  /*
  Option 2: Temperature is inversely proportional to age, with square root variance. This means that the particle
  cools down slowly once it is born.
 */
  if ((t - tstart) > 0.001)
    temperature = 100 / (float) pow((t - tstart + 0.005), 0.5);

  //   temperature = 1000 * pow((tstart-t+lifetime),2)/(lifetime*lifetime)-100;
  //cap the temperature
  temperature = (temperature > 1000 ? 1000 : temperature);

  /*
Upon being born, the particle first grows bigger, before it starts diminishing. This causes the fire near the source to be more opaque
and transparent as it moves up
 */

  if (!smaller) //20 milliseconds
    {
      size = size * 1.5f;
      if (size > 10)
        smaller = true;
    } else {
      //a nonlinear decay in size
      if (size > 1)
        size = size - 0.5f / size;
    }
}
