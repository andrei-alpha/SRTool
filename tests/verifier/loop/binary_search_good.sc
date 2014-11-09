// Binary search a given number

void main(int x) {
  assume(x >= 0 && x <= 100000);

  int left;
  int right;
  
  left = 0;
  right = 100000;

  while (left < right)
  inv(left <= x)
  inv(right >= x)
  {
    int mid;
    mid = (left + right) / 2;

    if (mid <= x) {
      right = mid;
    } else {
      left = mid + 1;
    }
  }

  assert(left == x);
  assert(right == x);
  assert(left == right);
}
