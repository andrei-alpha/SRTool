// Test if a number is Carmichael
// Carmichael numbers: 561, 1105, 1729, 2465, 2821
// 6601, 8911, 10585, 15841, 29341, 41041, ...
// Don't run in bmc mode as unwinding assertion will be added

void main(int n) {
  // n = 29341; This would fail
  assume(n >= -1000000000);  

  int isCarmichael;
  isCarmichael = 1;

  int a;
  a = n - 1;
  while (a > 1) {
    int an;
    an = 1;

    int i;
    i = 0;
    while (i < n) {
      // Otherwise we overflow
      if (a < 46000)
        an = (an * a) % n;
      i = i + 1;
    }
    
    if (an != a)
      isCarmichael = 0;

    // Check if n is prime each time    
    int isPrime;
    int d;
    isPrime = 0;    

    d = 2;
    while (d * d <= n) {
      if (!(n % d))
        isPrime = 0;
      d = d + 2;
    }

    if (isPrime)
      isCarmichael = 0; 
    a = a - 1;
  }

  // Assert there isn't any carmichael number over 20000
  assert(!isCarmichael || n < 20000); 
} 
