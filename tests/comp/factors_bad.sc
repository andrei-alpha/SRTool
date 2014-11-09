// Integer factorisation

void main(int n) {
//  To make it small
  assume(n <= 10 && n >= 1); 

  int ninit;
  int d;
  int m;
  d = 2;
  m = 1;

  while (d * d <= n) {
    
    int pow;
    pow = 0;

    while (!(n % d)) {
      n = n / d;
      pow = pow + 1;
    }

    while (pow > 0) {
      m = m * d;
      pow = pow - 1;
    } 

    d = d + 1;
  }

  assert(ninit == m);
}
