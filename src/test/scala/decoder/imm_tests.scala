package adept.decoder.tests.imm

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import adept.decoder._
import adept.alu.AluOps
import adept.core.AdeptControlSignals

////////////////////////////////////////////////
// Test Suite for Immediate Type instructions
////////////////////////////////////////////////
class SLLI(c: InstructionDecoder) extends DecoderTestBase(c) {
  private def SLLI(rs1: Int, imm: Int, rd: Int) {
    val instr = ((imm << 20) | ((31 & rs1) << 15) | (slli << 12) | ((31 & rd) << 7) | op_code.Immediate.litValue())   
    val shamt = 31 & imm
    val new_imm = if ((imm >> 11) == 1)
                    ((0xFFFFF << 12) | imm)
                  else 
                    imm
    val trap = if ((imm >> 5) != 0)
                 1
               else
                 0
    poke(c.io.stall_reg, false)                 
    poke(c.io.basic.instruction, instr)

    step(1)

    expect(c.io.basic.out.registers.we, true)
    expect(c.io.basic.out.registers.rsd_sel, rd)
    expect(c.io.basic.out.registers.rs1_sel, rs1)
    expect(c.io.basic.out.registers.rs2_sel, shamt)
    expect(c.io.basic.out.immediate, new_imm)
    expect (c.io.basic.out.trap, trap)
    expect(c.io.basic.out.alu.op, AluOps.sll)
    expect(c.io.basic.out.sel_rf_wb, AdeptControlSignals.result_alu)
    expect(c.io.basic.out.sel_operand_a, AdeptControlSignals.sel_oper_A_rs1)
    expect(c.io.basic.out.sel_operand_b, AdeptControlSignals.sel_oper_B_imm)
  }

  for (i <- 0 until 100) {
    var rs1 = rnd.nextInt(32)
    var imm = rnd.nextInt(4096)
    val rd  = rnd.nextInt(32)
    
    SLLI(rs1, imm, rd)
  }
}

