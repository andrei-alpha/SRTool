
void main(int n) {
  int x;
  x = 0;

  if (n) {
    int b;
    assume(b > 0);
    x = b;
  }
  if (n) {
    int b;
    assume(b > 0);
    assume(b != x);
    x = x + b;
    assert(x > 1);
  }
  assert(!(x % 2));
}
