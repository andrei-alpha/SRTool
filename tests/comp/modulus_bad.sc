// https://graphics.stanford.edu/~seander/bithacks.html#ModulusDivisionEasy */
// Compute modulus division by (1 << s) - 1 without division

void main() {

  int n;    /* numerator */
  assume(n > 0);  

  int s;    /* s > 0 */
  assume(s > 0 && s < 30);

  int d;    
  int m;    /* n % d goes here. */

  d = (1 << s) - 1; /* so d is either 1, 3, 7, 15, 31, ...) */

  if (d > 0) {
    m = n;
    while (n > d) {
      m = 0;
    
      while (n > 0) {
        m = m + (n & d);
        n = n >> s;
      }
      // Put a more subtle bug!
      n = m - 1;
    }
    
    /* Now m is a value from 0 to d, but since with modulus division
      * we want m to be 0 when it is d. */
    
    if (m == d) {
      m = 0;
    }

    assert(m == n % d);
 }
}
