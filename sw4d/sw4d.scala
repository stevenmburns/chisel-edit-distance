package SmithWaterman

import scala.collection.mutable.HashMap
import scala.util.Random

import Chisel._ 

class sw4d() extends Module {

  private def Max2(x : SInt, y : SInt) = Mux(x > y, x, y)
  private def Min2(x : SInt, y : SInt) = Mux(x < y, x, y)

  val io = new Bundle {
    val select_ms = UInt( INPUT, 2)
    val L = SInt( INPUT, 4)
    val U = SInt( INPUT, 4)
    val Y = UInt( INPUT, 4)
    val Z = UInt( INPUT, 4)
    val Lp = SInt( OUTPUT, 4)
    val Up = SInt( OUTPUT, 4)
    val Yp = UInt( OUTPUT, 4)
    val Zp = UInt( OUTPUT, 4)
  }
  val fy = Max2( SInt(0), SInt(5,width=5)-io.Y)
  val fz = Max2( SInt(0), SInt(5,width=5)-io.Z)
  val sb = io.L+fy
  val sc = io.U+fz
  val v = Vec( SInt(2,width=5), SInt(5,width=5), SInt(7,width=5))
  val ms = v(io.select_ms)
  val sd = Max2( ms, Max2( sb, sc)) // Five bits
  val d = sd - UInt(6)
  io.Lp := d - io.U
  io.Up := d - io.L
  val tmp = SInt(width=6)
  tmp := sd
  io.Yp := Min2( SInt(5), tmp - sb)
  io.Zp := Min2( SInt(5), tmp - sc)
}

class sw4dTests(c: sw4d) extends Tester(c, Array(c.io)) {
  def sw4dRef( select_ms : Int,
               L : Int,
               U : Int,
               Y : Int,
               Z : Int) : List[Int] = {

    def min( x: Int, y: Int) = if ( x < y) x else y
    def max( x: Int, y: Int) = if ( x > y) x else y
    def max3( x: Int, y: Int, z: Int) = max( x, max( y, z))

    val fy = max( 0, 5-Y)               // max(0,5) = 5
    val fz = max( 0, 5-Z)               // max(0,5) = 5
    val sb = L+fy                       // -1
    val sc = U+fz                       // -1
// 2,5,7 or -4,-1,1 + 6
    val ms = List( 2, 5, 7)( select_ms) // 2
    val sd = max3( ms, sb, sc)          // 2
    val d = sd - 6                      // -4
    val Lp = d - U                      // -4 - -6 = 2
    val Up = d - L                      // -4 - -6 = 2
    val Yp = min( 5, sd - sb)           // 2 - -1 = 3
    val Zp = min( 5, sd - sc)           // 2 - -1 = 3
    List( Lp, Up, Yp, Zp)
  }

  def cartesianProduct[T](xss: List[List[T]]): List[List[T]] = xss match {
     case Nil => List(Nil)
     case h :: t => for( xh<-h; xt<-cartesianProduct(t)) yield xh::xt
  }

  defTests {
    var allGood = true

    val vars = new HashMap[ Node, Node]()

    val ms_range = (0 until 3).toList
    val lu_range = (-6 until 13).toList
    val yz_range = (0 until 6).toList
    val cp = cartesianProduct( List( ms_range, lu_range, lu_range, yz_range, yz_range))

    for (lst <- cp) {
       val ms_select = lst(0)
       val L         = lst(1)
       val U         = lst(2)
       val Y         = lst(3)
       val Z         = lst(4)
       val res = sw4dRef( ms_select, L, U, Y, Z)
       println( ms_select, L, U, Y, Z, res)
/*
       vars.clear()
       vars(c.io.select_ms) = UInt(ms_select,width=2)
       vars(c.io.L) = SInt(L,width=4)
       vars(c.io.U) = SInt(U,width=4)
       vars(c.io.Y) = UInt(Y,width=4)
       vars(c.io.Z) = UInt(Z,width=4)
       vars(c.io.Lp) = SInt(res(0),width=4)
       vars(c.io.Up) = SInt(res(1),width=4)
       vars(c.io.Yp) = UInt(res(2),width=4)
       vars(c.io.Zp) = UInt(res(3),width=4)
       allGood = step(vars) && allGood
*/
    }
    allGood
  }
}

object sw4d {
  def main(args: Array[String]): Unit = {
    val args = Array("--backend", "c", "--genHarness", "--compile", "--test")
//    val args = Array("--backend", "v", "--genHarness")
    chiselMainTest(args, () => Module(new sw4d())) {
      c => new sw4dTests(c) }
  }
}
