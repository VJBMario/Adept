package adept.alu.br

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import adept.alu._
import adept.decoder.OpCodes

////////////////////////////////////////////////
// Test Suite for Branch Type instructions
////////////////////////////////////////////////
class BEQ_BNE(c: ALU) extends ALUTestBase(c) {
  private def BEQ_BNE(rs1: Int, rs2: Int) {
    poke(c.io.in.operand_A, rs1)
    poke(c.io.in.operand_B, rs2)
    poke(c.io.in.decoder_params.op, alu_ops.sub)
    step(1)
    expect(c.io.result, rs1 - rs2)
  }

  for (i <- 0 until 10000) {
    var rs1 = rnd.nextInt(2000000000)
    var rs2 = rnd.nextInt(2000000000)

    val signedness = rnd.nextInt(50)
    if (signedness % 4 == 0) {
      rs1 = rs1 * -1
      rs2 = rs2 * -1
    } else if (signedness % 5 == 0) {
      rs2 = rs2 * -1
    } else if (signedness % 2 == 0) {
      rs1 = rs1 * -1
    }

    BEQ_BNE(rs1, rs2)
  }
}

class BLT_BGE(c: ALU) extends ALUTestBase(c) {
  private def BLT_BGE(rs1: Int, rs2: Int) {
    poke(c.io.in.operand_A, rs1)
    poke(c.io.in.operand_B, rs2)
    poke(c.io.in.decoder_params.op, alu_ops.slt)
    step(1)
    expect(c.io.result, rs1 < rs2)
  }

  for (i <- 0 until 10000) {
    var rs1 = rnd.nextInt(2000000000)
    var rs2 = rnd.nextInt(2000000000)

    val signedness = rnd.nextInt(50)
    if (signedness % 4 == 0) {
      rs1 = rs1 * -1
      rs2 = rs2 * -1
    } else if (signedness % 5 == 0) {
      rs2 = rs2 * -1
    } else if (signedness % 2 == 0) {
      rs1 = rs1 * -1
    }

    BLT_BGE(rs1, rs2)
  }
}

class BLTU_BGEU(c: ALU) extends ALUTestBase(c) {
  private def BLTU_BGEU(rs1: Int, rs2: Int) {
    // Turns out Scala doesn't have unsigned types so we do this trickery
    val u_rs1 = rs1.asInstanceOf[Long] & 0x00000000ffffffffL
    val u_rs2 = rs2.asInstanceOf[Long] & 0x00000000ffffffffL
    poke(c.io.in.operand_A, rs1)
    poke(c.io.in.operand_B, rs2)
    poke(c.io.in.decoder_params.op, alu_ops.sltu)
    step(1)
    expect(c.io.result, u_rs1 < u_rs2)
  }

  for (i <- 0 until 10000) {
    var rs1 = rnd.nextInt(2000000000)
    var rs2 = rnd.nextInt(2000000000)

    val signedness = rnd.nextInt(50)
    if (signedness % 4 == 0) {
      rs1 = rs1 * -1
      rs2 = rs2 * -1
    } else if (signedness % 5 == 0) {
      rs2 = rs2 * -1
    } else if (signedness % 2 == 0) {
      rs1 = rs1 * -1
    }

    BLTU_BGEU(rs1, rs2)
  }
}
