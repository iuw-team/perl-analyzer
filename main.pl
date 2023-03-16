#!/usr/bin/env perl
use strict;
use warnings;
my %table = (key_0 => 'value_0',
             key_1 => 'value_1',
             key_2 => (1, 3, 5));
foreach (%table){
    for(my $i = 1; $i < 10; $i += 1){
        for(my $j = 1; $j < 100; $j += 3){
            my $state = 0;
            my $temp;
            for(my $k = 2; $k < 1000 && $state != 1; $k += 1){
                $temp = hardCalc($i, $j, $k);
                if($temp > 100){
                    $temp = 1;
                }
                elsif ($temp > 50){
                    print "Almost'";
                    if($temp > 55){
                        print "Almost-Almost\n"; }
                    if($temp < 55){
                        print "Not so Almost"; }
                    else {print "Enough";}
                }
                else {
                    print "Not now";
                }
            }
        }
    }
}