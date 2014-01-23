package SmithWaterman

import scala.collection.mutable.HashMap
import scala.util.Random

import Chisel._ 

class sw4d() extends Module {

  private def Max2(x : SInt, y : SInt) = Mux(x > y, x, y)
  private def Min2(x : SInt, y : SInt) = Mux(x < y, x, y)

  val io = new Bundle {
    val select_ms = UInt( INPUT, 2)
    val l = SInt( INPUT, 5)
    val u = SInt( INPUT, 5)
    val y = UInt( INPUT, 3)
    val z = UInt( INPUT, 3)
    val lp = SInt( OUTPUT, 5)
    val up = SInt( OUTPUT, 5)
    val yp = UInt( OUTPUT, 3)
    val zp = UInt( OUTPUT, 3)
  }
  val fy = Max2( SInt(0), SInt(5,width=5)-io.y)
  val fz = Max2( SInt(0), SInt(5,width=5)-io.z)
  val sb = io.l+fy
  val sc = io.u+fz
  val v = Vec( SInt(2,width=5), SInt(5,width=5), SInt(7,width=5))
  val ms = v(io.select_ms)
  val sd = Max2( ms, Max2( sb, sc)) // Five bits
  val d = sd - UInt(6)
  io.lp := d - io.u
  io.up := d - io.l
  val tmp = SInt(width=6)
  tmp := sd
  io.yp := Min2( SInt(5), tmp - sb)
  io.zp := Min2( SInt(5), tmp - sc)
}

class sw4dTests(c: sw4d) extends Tester(c, Array(c.io)) {
  def sw4dRef( select_ms : Int,
               l : Int,
               u : Int,
               y : Int,
               z : Int) : List[Int] = {

    def min( x: Int, y: Int) = if ( x < y) x else y
    def max( x: Int, y: Int) = if ( x > y) x else y
    def max3( x: Int, y: Int, z: Int) = max( x, max( y, z))

    val fy = max( 0, 5-y)               // max(0,5) = 5
    val fz = max( 0, 5-z)               // max(0,5) = 5
    val sb = l+fy                       // -1
    val sc = u+fz                       // -1
// 2,5,7 or -4,-1,1 + 6
    val ms = List( 2, 5, 7)( select_ms) // 2
    val sd = max3( ms, sb, sc)          // 2
    val d = sd - 6                      // -4
    val lp = d - u                      // -4 - -6 = 2
    val up = d - l                      // -4 - -6 = 2
    val yp = min( 5, sd - sb)           // 2 - -1 = 3
    val zp = min( 5, sd - sc)           // 2 - -1 = 3
    List( lp, up, yp, zp)
  }

  def cartesianProduct[T](xss: List[List[T]]): List[List[T]] = xss match {
     case Nil => List(Nil)
     case h :: t => for( xh<-h; xt<-cartesianProduct(t)) yield xh::xt
  }

  defTests {
    var allGood = true

    val vars = new HashMap[ Node, Node]()

    val ms_range = (0 until 3).toList
    val lu_range = (-5 until 5).toList
    val yz_range = (0 until 6).toList
    val cp = cartesianProduct( List( ms_range, lu_range, lu_range, yz_range, yz_range))

    for (lst <- cp) {
       val List( ms_select, l, u, y, z) = lst
       val res = sw4dRef( ms_select, l, u, y, z)
       println( ms_select, l, u, y, z, res)
       vars.clear()
       vars(c.io.select_ms) = UInt(ms_select,width=2)
       vars(c.io.l) = SInt(l,width=5)
       vars(c.io.u) = SInt(u,width=5)
       vars(c.io.y) = UInt(y,width=3)
       vars(c.io.z) = UInt(z,width=3)
       vars(c.io.lp) = SInt(res(0),width=5)
       vars(c.io.up) = SInt(res(1),width=5)
       vars(c.io.yp) = UInt(res(2),width=3)
       vars(c.io.zp) = UInt(res(3),width=3)
       allGood = step(vars) && allGood
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
