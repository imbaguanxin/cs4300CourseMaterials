#version 440 core

out vec4 fColor;
uniform vec2 dims;
uniform vec2 center;
uniform float scale;
uniform int maxiter;

void main()
{
	int i;
	dvec2 z = vec2(gl_FragCoord[0],gl_FragCoord[1]);
	//convert to the actual window coordinates specified by center and scale

	z.x = scale*(z.x/dims.x - 0.5)+ center.x ;
	z.y = scale*(z.y/dims.y - 0.5)+ center.y ;
	dvec2 cl = z; /* for mandelbrot */
//	vec2 cl = vec2(-0.7017,-0.3842); /* for julia 1*/
//		vec2 cl = vec2(-0.8,0.156); /* for julia 2*/
//		vec2 cl = vec2(-0.4,0.6); /* for julia 3*/
	vec3 colors[7];

	colors[0] = vec3(0,0,0);
	colors[1] = vec3(0,0,1);
	colors[2] = vec3(0,1,1);
	colors[3] = vec3(0,1,0);
	colors[4] = vec3(1,1,0);
	colors[5] = vec3(1,0,0);
	colors[6] = vec3(0,0,0);

	z = vec2(0,0); //for mandelbrot only, comment out for others
	
	for (i=0;i<maxiter;i++)
	{
		double x;
		double y;

		x = (z.x*z.x - z.y*z.y) + cl.x;
		y = (z.x*z.y + z.y*z.x) + cl.y; //mandelbrot only, comment for others
	//  y = 2*z.x*z.y + cl.y; //for everything except mandelbrot
	

		if ((x*x+y*y)>4.0)
			break;

		z.x = x;
		z.y = y;
	}

    if (i==maxiter)
		fColor = vec4(0,0,0,1);
	else
	{
	//	fColor = vec4(1,1,1,1);
	//	fColor = vec4(1.0*i/maxiter,1.0*i/maxiter,1.0*i/maxiter,1);
		float incr = 6.0*i/maxiter;
		if (incr>=6)
		{
			incr = 5.999;
		}
		
		int c = int(floor(incr));

		fColor = vec4(mix(colors[c],colors[c+1],incr-c),1.0);
		
		
	}
	
	
}
