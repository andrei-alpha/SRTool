// Fermant's last theorem a^n + b^n = c^n has solutions for n <= 2

void main(int a, int b, int c, int n) {
  assume(a > 10 && a < 100);
  assume(b > 10 && b < 100);
  assume(c > 10 && c < 100);
  assume(a != b);
  assume(b != c);
  assume(n > 1);  

  int ap;
  int bp;
  int cp;

  ap = 1;
  bp = 1;
  cp = 1;

  int i;
  
  i = n;
  while (i > 1) {
    assume(ap < 99999999);
    assume(bp < 99999999);
    assume(cp < 99999999);

    ap = ap * a;
    bp = bp * b;
    cp = cp * c;
    i = i - 1;
  }

  assert(ap + bp != cp);
}
