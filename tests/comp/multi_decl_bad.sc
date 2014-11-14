// We assume here that b takes the same value, false of course

void main(int v) {
  int x;
  x = 0;

  if (v > 20) {
    int b;
    assume(b >= 0 && b < 100000000);
    x = x + b;
  }
  if (v > 30) {
    int b;
    assume(b >= 0 && b < 100000000);
    x = x + b;
  }
  if (v > 40) {
    int b;
    assume(b >= 0 && b < 100000000);
    x = x + b;
  }
  if (v > 50) {
    int b;
    assume(b >= 0 && b < 100000000);
    x = x + b;
  }

  assert(!(v > 50) || !(x % 4));
  assert(!(v > 40 && v < 50) || !(x % 3));
  assert(!(v > 30 && v < 40) || !(x % 2));
  assert(!(v > 20 && v < 30) || !(x % 1));
}
