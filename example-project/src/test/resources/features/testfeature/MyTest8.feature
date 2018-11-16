# language: ro
Funcţionalitate: Bună ziua

  Scenariu: Mulțumesc foarte mult
    Date fiind Bună dimineața
    Cand Scuzați-mă!
    Atunci Vă doresc o zi plăcută!

  Structura scenariu: Mulțumesc foarte mult 2
    Date fiind Bună dimineața
    Cand Scuzați-mă <test>
    Atunci Vă doresc o zi plăcută!

    Exemple:
      | test |
      | 1    |
      | 2    |