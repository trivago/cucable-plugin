# language: pl
Funkcja: Test

  Scenariusz: Testowy scenariusz
    Gdy: To jest krok pierwszy

  Scenariusz: Drugi testowy scenariusz
    Gdy: To jest krok pierwszy
    Kiedy: Robie krok drugi
    Wtedy: Oczekuje kroku trzeciego

  Scenariusz: To jest testowy scenariusz bez krokow

  Scenariusz: To jest scenariu z data table
    Gdy: To jest krok pierwszy
    Kiedy: szukam
  | panstwo | Niemcy |
  | miasto  |        |
    Wtedy: Widze wyniki wyszukiwania z Niemiec