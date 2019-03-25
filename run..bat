cd ./
set mydate=%date:~10,4%%date:~4,2%%date:~7,2%
lein run attendance/%mydate%/ "Bronx%%20North" Dallas Manhattan Arlington "Bronx%%20South" "North%%20Dallas" Newark Worcester Boston 