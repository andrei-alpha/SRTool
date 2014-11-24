// Fermant`s Last Theorem  states that no three positive integers a, b, and c
// can satisfy the equation a^n + b^n = c^n for any integer value of n greater
// than two.

void main(int a, int b, int c, int n) {
  assume(a >= 1 && a <= 1000);
  assume(b >= 1 && b <= 1000);
  assume(c >= 1 && c <= 1000);
  assume(n == 3);

  int an;
  int bn;
  int cn;
  int i;

  i = 1;
  while (i <= n) {
    an = an * a;
    bn = bn * b;
    cn = cn * c;
    
    i = i + 1;

    assert(an > 1);
    assert(bn > 1);
    assert(cn > 1);
  }

  // We shouldn't reach this loop
  while (an + bn == cn) {
    an = an - 1;
    bn = bn - 1;
    cn = cn - 1;

    assert(0);
  }

  assert(an == a * a * a);
  assert(bn == b * b * b);
  assert(cn == c * c * c);
}
