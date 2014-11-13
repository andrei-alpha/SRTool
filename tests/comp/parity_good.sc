
void main(int v) {
  assume(v > 0);

  int parity1;
  int parity2;
  int v1;
  int v2;

  v1 = v;
  parity1 = 0;
  while(v1 != 0) {
    parity1 = parity1 ^ 1;
    v1 = v1 & (v1 - 1);
  }

  v2 = v;
  parity2 = 0;
  v2 = v2 ^ (v2 >> 1);
  v2 = v2 ^ (v2 >> 2);
  v2 = (v2 & 286331153) * 286331153; 
    
  if (((v2 >> 28) & 1) == 0) {
    parity2 = 0;
  } else {
    parity2 = 1;
  }
  

  assert(parity1 == parity2);
}
