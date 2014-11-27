
void main(int n) {
  int i;
  
  // constants
  int a;
  int b;
  assume(a > 0 && a < 10000);
  assume(b > 0 && b < 10000);

  i = 0;
  int sumr;
  sumr = 0;
  while(i < n) {
    int x;
    assume(x > 0 && x < 10000);

    x = a * 2 + b * 4 + (((x + 1) + 2) - 3) * 2;
    sumr = sumr + x % 2;

    i = i + 1;
  }

  assert(sumr == 0);
}
