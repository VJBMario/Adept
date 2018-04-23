// See LICENSE for license details.
package adept.pc

import chisel3._
import chisel3.util._

import adept.config.AdeptConfig
class BranchOpConstants { // join this group with all the rest of the configurations
  // Branch Type
  val BR_NE    = 1.asUInt(3.W)  // Branch on NotEqual
  val BR_EQ    = 0.asUInt(3.W)  // Branch on Equal
  val BR_GE    = 5.asUInt(3.W)  // Branch on Greater/Equal
  val BR_GEU   = 7.asUInt(3.W)  // Branch on Greater/Equal Unsigned
  val BR_LT    = 4.asUInt(3.W)  // Branch on Less Than
  val BR_LTU   = 6.asUInt(3.W)  // Branch on Less Than Unsigned

  val BR_Cond  = "b1100011".U
  val BR_JAL   = "b1101111".U
  val BR_JALR  = "b1100111".U
}

class Pc(config: AdeptConfig, br: BranchOpConstants) extends Module{
  val io = IO(new Bundle {
    // flags for branch confirmation
    val br_flags   = Input(Bool()) // branch verification flag
    // In from decoder
    val in_opcode  = Input(UInt((config.op_code + config.funct).W)) // opecode(7 bits) + function(3 bits) from word
    // Jump Adress for JALR
    val br_step    = Input(SInt(config.XLen.W)) // In case of JALR
    // Offset for JAL or Conditional Branch
    val br_offset  = Input(SInt(config.XLen.W))
    // Program count after 1st pipeline level
    val pc_in      = Input(UInt(config.XLen.W))
    // Memory stall control signal
    val mem_stall  = Input(Bool())
    // Memory enable control signal
    val mem_en     = Input(Bool())
    // Stall delayed by 1 clock
    val stall_reg  = Output(Bool())
    // Program count to be sent for calc of new PC or for storage
    val pc_out     = Output(UInt(config.XLen.W))
  })

  // Conditional Branch verification and flags attribution
  val cond_br_ver = MuxLookup (io.in_opcode (9,7), false.B,
    Array(br.BR_EQ  -> ~io.br_flags,
          br.BR_NE  -> io.br_flags,
          br.BR_LT  -> io.br_flags,
          br.BR_GE  -> ~io.br_flags,
          br.BR_LTU -> io.br_flags,
          br.BR_GEU -> ~io.br_flags))

  val cond_br_exe     = (io.in_opcode(6, 0) === br.BR_Cond) & cond_br_ver

  // Offset selection criteria: is it a JAL? or is it Conditional with correct flags?
  val offset_sel      = (io.in_opcode(6, 0) === br.BR_JAL) | cond_br_exe

  // Auxiliar variable that contains either offset or 1
  val add_to_pc_val   = Mux(offset_sel, io.br_offset.asUInt, 4.U)
  // JALR condition verification
  val jalr_exec       = (io.in_opcode(6, 0) === br.BR_JALR)
  // next pc calculation
  val progCount       = RegInit("h1000_0000".U.asSInt)
  val next_pc         = Mux(offset_sel, io.pc_in, progCount.asUInt).asSInt + add_to_pc_val.asSInt
  // Remove LSB for JALR
  val jalr_value      = Cat(io.br_step(config.XLen - 1, 1), false.B).asSInt
  val jalrORpc_select = Mux(jalr_exec, jalr_value, next_pc)

  // Logic to stall the next PC
  val stall           = RegInit(false.B)
  stall              := offset_sel | jalr_exec
  io.stall_reg       := stall

  // Delay memory enable by one cycle
  val mem_en_reg = RegInit(false.B)
  mem_en_reg := io.mem_en

  // PC update
  when (!stall && !io.mem_stall && (mem_en_reg ^ !io.mem_en)) {
    progCount := jalrORpc_select
  }

  io.pc_out  := progCount.asUInt
}
