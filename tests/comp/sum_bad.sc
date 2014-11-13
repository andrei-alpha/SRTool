
void main(int n) {
  assume(n < 40000 && n >= 0);

  int sn;
  int i;
  i = 0;
  sn = 0;
  
  while(i <= n) {
    sn = sn + i;
    i = i + 1;
  }
  
  // Put a bug here
  if (n > 30000)
    sn = sn / 2;

  assert(sn == n*(n+1)/2);
}
