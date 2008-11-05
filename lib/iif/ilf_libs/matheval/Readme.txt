Java Math Expression Evaluator
Freeware V1.01 
by The-Son LAI 
Lts@writeme.com 
http://Lts.online.fr 
______________________________________________________________

V 1.01 (18/10/01) added rnd function : rnd(7) will return a number between 0 and 6.99...
V Singh (16/10/01) customized version by Hardeep Singh [download].

______________________________________________________________

This jar contains a class that evaluates math expressions.
See the javadoc for more informations. 
See the source too. If you modify it, please send me a copy of the modified version.
Check the latest version to download.


Usage: 
java MathEvaluator [your math expression] 

Ex: 
java -cp meval.jar com.primalworld.math.MathEvaluator -cos(0)*(1+2) 
java -cp meval.jar com.primalworld.math.MathEvaluator .05*200+3.01

Don't put any space in the expression in command line. 
If you want to use the math evaluator in a java code do:

import com.primalworld.math.MathEvaluator;
...
MathEvaluator m = new MathEvaluator("-5-6/(-2) + sqr(15+x)");
m.addVariable("x", 15.1d);
System.out.println( m.getValue() );
 
Spaces are allowed there.