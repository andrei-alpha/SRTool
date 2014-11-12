/* This tests assumes that if we choose 3 random numbers that are not equal
and have a smith number as multiple their remaining prime divisor from factorisation
will be different always which is not true for the input below. */

void main(int v1, int v2, int v3) {
/*
  v1 = 378 * 641;
  v2 = 576 * 641;
  v3 = 648 * 641;
*/
  assume(v1 > 1 && v2 > 1 && v3 > 1);
  assume(v1 != v2 && v2 != v3 && v1 != v3);  

  int d;
  int p1;
  int sp1; 
  int p2;
  int sp2;
  int p3;
  int sp3;
  int temp;  

  p1 = 1;
  sp1 = 0;
  p2 = 1;
  sp2 = 0;
  p3 = 1;
  sp3 = 0;

  d = 2;
  while (d * d <= v1 || d * d <= v2 || d * d <= v3) {

    while(!(v1 % d)) {
      v1 = v1 / d;
      p1 = p1 * d;
      
      temp = d;
      while(temp) {
        sp1 = sp1 + (temp % 10);
        temp = temp / 10;
      }
    }

    while (!(v2 % d)) {
      v2 = v2 / d;
      p2 = p2 * d;

      temp = d;
      while(temp) {
        sp2 = sp2 + (temp % 10);
        temp = temp / 10;
      }
    }

    while (!(v3 % d)) {
      v3 = v3 / d;
      p3 = p3 * d;
      
      temp = d;
      while(temp) {
        sp3 = sp3 + (temp % 10);
        temp = temp / 10;
      }
    }

    d = d + 2;
  }

  int ssp1;
  int ssp2;
  int ssp3;
  int t1;
  int t2;
  int t3;  

  ssp1 = 0;
  ssp2 = 0;
  ssp3 = 0;

  t1 = p1;
  t2 = p2;
  t3 = p3;
  while(t1 || t2 || t3) {
    ssp1 = ssp1 + (t1 % 10);
    ssp2 = ssp2 + (t2 % 10);
    ssp3 = ssp3 + (t3 % 10);
    t1 = t1 / 10;
    t2 = t2 / 10;
    t3 = t3 / 10;
  }

  assume(v1 != 1 || v2 != 1 || v3 != 1);
  assume(p1 != 1 || p2 != 1 || p3 != 1);
  assume(ssp1 == sp1 && ssp2 == sp2 && ssp3 == sp3);

  assert(v1 != v2 || v2 != v3);
}
