#!/usr/bin/env perl
use strict;
use warnings;
open(my $in, "<", "user.back") or die "Can't open data file";
my $lastUser;
$lastUser = <$in>;
print "Enter your name: ";
my $userName = <STDIN>;
chomp $userName;
while(length($userName) <= 3){
    print "Sorry, but your name is too short. Try again!\n";
    $userName = <STDIN>;
    chomp $userName;
}
print "Hello, $userName!\n";
my $userPet;
if($userName eq $lastUser){
    print "Your last session is saved, $userName";
    $userPet = <$in>;
}
else {
    print "Input your favourite pet ";
    $userPet = <STDIN>;
}
my @animals = ("camel", "llama", "owl");
push @animals, $userPet;
close($in);
#for(my $i=9; $i != 0; $i-=1){
#    print "$i\n";
#    if($i == 4){
#        print "Almost";
#    }
#    else {
#        print "Other branch";
#    }
#}
#my $other = <$in>;
#my $longString = "Hello $userName! How your $other";
#my $x = @animals;
#my $y = getHugeNumber();
#my $value = shift @animals;
#unshift @animals, $other;
#if (($x < 11) || ($y > 11)){
#    if($y < 11 && $z > 43){
#        print"Let's me do some magic";
#    }
#}