// Miller-Rabin primality testing with base 2 fails for pseudoprimes like:
// 2047, 3277, 4033, 4681, 8321, 15841, 29341, 42799

void main(int p) {
  int isPrime;
  int iterations;
  
  // With 2 iterations we have base 2,3 and the first failure is 1,373,653  
  // Check for sure for numbers less than 10,000 but intentionally fail for
  // 15841, 29341 and 42799
  if (p < 10000)
    iterations = 2;
  else
    iterations = 1;
    
  isPrime = 1;

  // Cannot check for numbers > 46335
  if (p > 46335)
    isPrime = -1;

  if (p < 2)
    isPrime = 0;
  if (p != 2 && p % 2 == 0)
    isPrime = 0;

  int s;
  s = p - 1;

  while (s % 2 == 0) {
    s = s / 2;
  }
   
  int i;
  i = 0;
  while (i < iterations && isPrime) {
    int temp;
    int a;

    temp = s;
    // Take a prime    
    if (i == 0)
      a = 2;
    else {
      if (i == 1)
        a = 3;
      else
        a = 5;
    }

    int mod;
    mod = 1;

    // Compute a^temp modulo p
    int j;
    j = 0;
    while (j < temp) {
      mod = (mod * a) % p;
      j = j + 1;
    }

    while(temp != p-1 && mod != 1 && mod != p-1) {
      mod = (mod * mod) % p;
      temp = temp * 2;
    }

    if (mod != p-1 && temp % 2 == 0)
      isPrime = 0;
  }

  // Check if the number is actually prime
  int d;
  d = 3;
  while(d * d <= p) {
    if (isPrime == 1)
      assert(p % d);
    if (isPrime == 0)
      assert(!(p % d));
    d = d + 2;
  }

  assert(1);
}
