`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    14:19:56 05/01/2013 
// Design Name: 
// Module Name:    top 
// Project Name: 
// Target Devices: 
// Tool versions: 
// Description: 
//
// Dependencies: 
//
// Revision: 
// Revision 0.01 - File Created
// Additional Comments: 
//
//////////////////////////////////////////////////////////////////////////////////

module max_short(
   input [15:0] x,
	input [15:0] y,
	output [15:0] z
);
assign z = ( y > x) ? y : x;
endmodule

module max_byte(
   input [7:0] x,
	input [7:0] y,
	output [7:0] z
);
assign z = ( y > x) ? y : x;
endmodule

module max_nibble(
   input [3:0] x,
	input [3:0] y,
	output [3:0] z
);
assign z = ( y > x) ? y : x;
endmodule

module fiveminusmax0(
   input [3:0] i,
	output [3:0] o
);

wire [4:0] fiveminus;
assign fiveminus = 5'd5 - {1'b0,i};
assign o = ( !fiveminus[4]) ? fiveminus[3:0] : 4'b0;
endmodule

module unit(
   input [1:0] select_ms,
   input [3:0] L, /* -6 <= L,U <= 7 */
	input [3:0] U,
	input [3:0] Y, /* 0 <= Y,Z <= 13 */
	input [3:0] Z,
	output [3:0] Lp,
	output [3:0] Up,
	output [3:0] Yp,
	output [3:0] Zp
);

/*
fy = max( 0, 5-y)
fz = max( 0, 5-z)
sb = L+fy
sc = U+fz
sd = max( ms+6,sb,sc)   ,   ms+6 in {2,5,7}
d = sd - 6
L' = d-U
U' = d-L
Y' = sd-sb
Z' = sd-sc
 */
wire [3:0] fy;
fiveminusmax0 gen_fy( .i(Y), .o(fy));
wire [3:0] fz;
fiveminusmax0 gen_fz( .i(Z), .o(fz));

wire [3:0] ms_plus_6;
assign ms_plus_6 = ( select_ms == 2'b00) ? 4'd2 : (( select_ms == 2'b01) ? 4'd5 : 4'd7);

wire [3:0] sb;
wire [3:0] sc;
wire [3:0] sd;
wire [3:0] d;
wire [3:0] sb_max_sc;
wire dummyy;
wire dummyz;
assign sb = L + fy;
assign sc = U + fz;
assign sb_max_sc = ( sb > sc) ? sb : sc;
assign sd = ( sb_max_sc > ms_plus_6) ? sb_max_sc : ms_plus_6;
assign d = sd - 4'd6;
assign Lp = d - U;
assign Up = d - L;
assign {dummyy,Yp} = {sd[3],sd} - {sb[3],sb};
assign {dummyz,Zp} = {sd[3],sd} - {sc[3],sc};

endmodule

module unitfull(
   input [1:0] select_ms,
   input [7:0] Hmm,
	input [7:0] Hmc,
	input [7:0] Hcm,
	input [7:0] Ecm,
	input [7:0] Fmc,
	output [7:0] Hcc,
	output [7:0] Ecc,
	output [7:0] Fcc
);

/*
Hcc = max( Hmm + ms, Ecc, Fcc)
Ecc = max( Hcm - 6, Ecm - 1)
Fcc = max( Hmc - 6, Fmc - 1)
 */

wire [7:0] Hcc0;
wire [7:0] Ecc0;
wire [7:0] Ecc1;
wire [7:0] Fcc0;
wire [7:0] Fcc1;

wire [7:0] ms;
assign ms = ( select_ms == 2'b00) ? -8'd4 : (( select_ms == 2'b01) ? -8'd1 : 8'd1);

assign Hcc0 = Hmm + ms;
assign Ecc0 = Hcm - 8'd6;
assign Ecc1 = Ecm - 8'd1;
assign Fcc0 = Hmc - 8'd6;
assign Fcc1 = Fmc - 8'd1;

wire [7:0] Ecc_max_Fcc;
assign Ecc_max_Fcc = ( Ecc > Fcc) ? Ecc : Fcc;
assign Hcc = ( Hcc0 > Ecc_max_Fcc) ? Hcc0 : Ecc_max_Fcc;

assign Ecc = ( Ecc0 > Ecc1) ? Ecc0 : Ecc1;
assign Fcc = ( Fcc0 > Fcc1) ? Fcc0 : Fcc1;

endmodule

module top(
    input USER_CLOCK,
    output [3:0] z
    );
	 
wire clk;
clockdrv clockdrv
   (// Clock in ports
    .CLK_IN1(USER_CLOCK),      // IN
    // Clock out ports
    .CLK_OUT1(clk));    // OUT	 
	 
reg [41:0] state;

assign z = state[27:24];
 
always @(posedge clk)
begin
   state = state + 1;
end

//wire [15:0] short1;
//wire [15:0] short0;
//assign { short1, short0} = state;

//wire [15:0] short_max;

//max_short max_short( .x(short0), .y(short1), .z(short_max));

wire [3:0] L;
wire [3:0] U;
wire [3:0] Y;
wire [3:0] Z;

wire [3:0] Lp;
wire [3:0] Up;
wire [3:0] Yp;
wire [3:0] Zp;

wire [1:0] select_ms;

unit unit( .select_ms(select_ms), .L(L), .U(U), .Y(Y), .Z(Z), .Lp(Lp), .Up(Up), .Yp(Yp), .Zp(Zp));

//wire [7:0] Hmm;
//wire [7:0] Hmc;
//wire [7:0] Hcm;
//wire [7:0] Ecm;
//wire [7:0] Fmc;
//wire [7:0] Hcc;
//wire [7:0] Ecc;
//wire [7:0] Fcc;
//unitfull unit( .select_ms(select_ms), .Hmm(Hmm), .Hmc(Hmc), .Hcm(Hcm), .Ecm(Ecm), .Fmc(Fmc), .Hcc(Hcc), .Ecc(Ecc), .Fcc(Fcc));


//wire [7:0] byte1;
//wire [7:0] byte0;
//assign {byte1, byte0} = state[15:0];
//wire [7:0] byte_max;
//max_byte max_byte( .x(byte0), .y(byte1), .z(byte_max));

//wire [3:0] nibble2;
//wire [3:0] nibble1;
//wire [3:0] nibble0;
//assign nibble2 = 4'd5;
//assign {nibble1,nibble0} = state[7:0];

//wire [3:0] nibble_max;
// wire [3:0] yy;

//max_nibble max_nibble( .x(nibble0), .y(nibble1), .z(nibble_max));

// assign yy = ( y > nibble2) ? y : nibble2;

wire [35:0] CONTROL0;
wire [35:0] CONTROL1;

wire [31:0] combout0;
wire [31:0] combout1;

wire [31:0] combin0;
wire [31:0] combin1;

assign {select_ms,L,U,Y,Z} = combin0[17:0];
assign combout0 = {16'b0,Lp,Up,Yp,Zp};
assign combout1 = 32'b0;
//assign {Hmc,Hcm,Ecm,Fmc} = combin0;
//assign {select_ms,Hmm} = combin1[9:0];

//assign combout0 = { 8'b0, Hcc, Ecc, Fcc};
//assign combout1 = 32'b0;

//assign {select_ms,Hmm,Hmc,Hcm,Ecm,Fmc} = state[41:0];

//wire [31:0] trig0;

//assign trig0 = { L, U, Y, Z, Lp, Up, Yp, Zp};
//assign trig0 = { 8'b0, Hcc, Ecc, Fcc};

icon2 icon2 (
    .CONTROL0(CONTROL0), // INOUT BUS [35:0]
    .CONTROL1(CONTROL1) // INOUT BUS [35:0]
);

vio vio0 (
    .CONTROL(CONTROL0), // INOUT BUS [35:0]
    .ASYNC_IN(combout0), // IN BUS [31:0]
    .ASYNC_OUT(combin0) // OUT BUS [31:0]
);

vio vio1 (
    .CONTROL(CONTROL1), // INOUT BUS [35:0]
    .ASYNC_IN(combout1), // IN BUS [31:0]
    .ASYNC_OUT(combin1) // OUT BUS [31:0]
);

endmodule
