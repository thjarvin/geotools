/*
 * Adjlong.java
 *
 * Created on 20 February 2002, 03:04
 */

package org.geotools.proj4j;

import java.util.StringTokenizer;
/**
 *
 * @author  James Macgill
 */
public class Misc implements Constants{
    
    
    
    
    /* reduce argument to range +/- PI */
    
    
    public static final double SPI =    3.14159265359;
    public static final double TWOPI =  6.2831853071795864769;
    public static final double ONEPI =  3.14159265358979323846;
    
    public static double adjlon(double lon) {
        if (Math.abs(lon) <= SPI) return( lon );
        lon += ONEPI;  /* adjust to 0..2pi rad */
        lon -= TWOPI * Math.floor(lon / TWOPI); /* remove integral # of 'revolutions'*/
        lon -= ONEPI;  /* adjust back to -pi..pi rad */
        return lon ;
    }
    
    //static final int MAX_WORK = 64;
    static final String sym = "NnEeSsWw";
    static final double[] vm = {
        .0174532925199433,
        .0002908882086657216,
        .0000048481368110953599
    };
    public static double dmsToR(String is){
        int n=0, nl;
        String work;
        double v, tv;
        String sign,s;
        boolean signFlag=true;
        
        //if (rs.length!=0)
        //	rs[0] = is;
        /* copy sting into work space */
        work = is.trim();
        StringTokenizer tok = new StringTokenizer(work,"Dd\'\"Rr+-"+sym,true);
        s = tok.nextToken();
        if (!(s.equals( "+") || s.equals("-"))){
            sign = "+";
            signFlag=false;
        }else{
            sign=s;
            signFlag=true;
        }
        for (v = 0., nl = 0 ; nl < 3 ; nl = n+1  ) {
            if(signFlag) {
                s = tok.nextToken();
            }else{
                signFlag=true;
            }
            
            //if its not a digit then what?
            try{
                tv= Double.parseDouble(s);
            }
            catch(NumberFormatException nfe){
                return Double.MAX_VALUE;
            }
            if(tok.hasMoreTokens()){
                s = tok.nextToken();//should be a seperator;
                if(s.equalsIgnoreCase("d")) {n=0;}
                else if(s.equals("\'")){ n=1;}
                else if(s.equals("\"")){ n=2;}
                else if(s.equalsIgnoreCase("r")){
                    if (nl!=0) {
                        //pj_errno = -16;
                        return Double.MAX_VALUE;
                    }
                    v = tv;
                    n = 4;
                    continue;
                }else{
                    // it must be a direction character
                    v+=tv * vm[n];
                    break;
                }
            }else{
                v+=tv * vm[n];
                break;
            }
            
            v += tv * vm[n];
            if(!tok.hasMoreTokens())return v;
        }
        /* postfix sign */
        char end = work.charAt(work.length()-1);
        if(sym.indexOf(end)>=0){
            sign = (sym.indexOf(end)>=4)?"-" : "+";
        }
        if (sign.equals("-")){
            v = -v;
        }
        return v;
    }
    
    
    /* determine small q */
    public static double qsfn(double sinphi, double e, double one_es) {
        double con;        
        if (e >= EPS) {
            con = e * sinphi;
            return (one_es * (sinphi / (1. - con * con) -
            (.5 / e) * Math.log((1. - con) / (1. + con))));
        } else
            return (sinphi + sinphi);
    }
    
    public static double msfn(double sinphi, double cosphi, double es) {
	return (cosphi / Math.sqrt (1. - es * sinphi * sinphi));
    }
}
