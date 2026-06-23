from decimal import Decimal
from pathlib import Path
import json


ROOT = Path("tmp/biz_data")
INSPECT_DIR = ROOT / "inspect"
CONFLICT_DIR = ROOT / "conflict"
STD_DOC_DIR = Path("/Users/weixuelei/workspace/aiAgent/ai-project/for-test/std-doc")

INSPECT_DIR.mkdir(parents=True, exist_ok=True)
CONFLICT_DIR.mkdir(parents=True, exist_ok=True)


BASE_INDICATORS = {
    "ind001": ("抗拉强度", "Rm", "PERFORMANCE", "MPa", "GB/T 228.1", "最大拉伸力/原始横截面积"),
    "ind002": ("延伸率", "A", "PERFORMANCE", "%", "GB/T 228.1", "断后伸长量/原始标距长度"),
    "ind003": ("屈服强度", "ReL", "PERFORMANCE", "MPa", "GB/T 228.1", "屈服点的应力值"),
    "ind004": ("厚度公差", "Δt", "DIMENSION", "mm", "千分尺测厚", "实测厚度与名义厚度之差"),
    "ind005": ("宽度公差", "Δw", "DIMENSION", "mm", "卷尺/自动测宽", "实测宽度与名义宽度之差"),
    "ind007": ("硬度", "HV", "PERFORMANCE", "HV", "GB/T 4340.1", "维氏硬度"),
    "ind008": ("n值", "n", "PERFORMANCE", "-", "拉伸应变硬化指数", "成形性能指标"),
    "td_ind_wdev": ("重量偏差", "TD_WDEV", "DIMENSION", "%", "理论重量差测定", "实测重量与理论重量偏差"),
    "td_ind_coat_mass": ("镀层重量", "TD_COAT", "PERFORMANCE", "g/m2", "三点称量法", "双面镀层重量"),
    "td_ind_ceq": ("碳当量", "TD_CEQ", "COMPOSITION", "%", "熔炼分析", "焊接性控制指标"),
    "td_ind_c": ("碳", "TD_C", "COMPOSITION", "%", "熔炼分析", "碳元素质量分数"),
    "td_ind_si": ("硅", "TD_SI", "COMPOSITION", "%", "熔炼分析", "硅元素质量分数"),
    "td_ind_mn": ("锰", "TD_MN", "COMPOSITION", "%", "熔炼分析", "锰元素质量分数"),
    "td_ind_p": ("磷", "TD_P", "COMPOSITION", "%", "熔炼分析", "磷元素质量分数"),
    "td_ind_s": ("硫", "TD_S", "COMPOSITION", "%", "熔炼分析", "硫元素质量分数"),
    "td_ind_cr": ("铬", "TD_CR", "COMPOSITION", "%", "熔炼分析", "铬元素质量分数"),
    "td_ind_ni": ("镍", "TD_NI", "COMPOSITION", "%", "熔炼分析", "镍元素质量分数"),
    "td_ind_p15_50": ("铁损P15/50", "TD_P15_50", "PERFORMANCE", "W/kg", "磁性能试验", "1.5T/50Hz单位铁损"),
    "td_ind_b50": ("磁感B50", "TD_B50", "PERFORMANCE", "T", "磁性能试验", "5000A/m磁感应强度"),
    "td_ind_kv2": ("冲击功KV2", "TD_KV2", "PERFORMANCE", "J", "夏比V型缺口冲击", "低温冲击吸收能量"),
}


def d(value):
    if value is None:
        return None
    return Decimal(str(value))


def ind(indicator_id, lower=None, upper=None, concession_lower=None, concession_upper=None):
    return {
        "id": indicator_id,
        "lower": d(lower),
        "upper": d(upper),
        "cl": d(concession_lower),
        "cu": d(concession_upper),
    }


DOCS = [
    {
        "seq": 1,
        "slug": "HRB400E_rebar",
        "pdf": "GB-T-SIM-1499-2026_HRB400E_rebar_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 1499-2026",
        "name": "HRB400E热轧带肋钢筋质量要求（模拟国标）",
        "variety": "热轧带肋钢筋",
        "grade": "HRB400E",
        "spec": "公称直径12mm-32mm",
        "sample_spec": "公称直径16mm，定尺12m",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 400, None, 392, None),
            ind("ind001", 540, None, None, None),
            ind("ind002", 16, None, 15, None),
            ind("td_ind_wdev", -6.0, 6.0, -6.5, 6.5),
        ],
    },
    {
        "seq": 2,
        "slug": "Q235B_cold_rolled_national",
        "pdf": "GB-T-SIM-912-2026_Q235B_cold_rolled_sheet_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 912-2026",
        "name": "碳素结构钢冷轧薄板及钢带质量要求（模拟国标）",
        "variety": "冷轧板",
        "grade": "Q235B",
        "spec": "厚度0.50mm-3.00mm，宽度600mm-1500mm",
        "sample_spec": "厚度1.50mm，宽度1000mm",
        "version": "2026版",
        "effective": "2026-03-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind001", 370, 510, 360, 520),
            ind("ind002", 26, None, None, None),
            ind("ind003", 235, None, 225, None),
            ind("ind004", -0.120, 0.120, -0.150, 0.150),
        ],
    },
    {
        "seq": 3,
        "slug": "Q355B_hot_rolled_plate",
        "pdf": "GB-T-SIM-3274-2026_Q355B_hot_rolled_plate_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 3274-2026",
        "name": "Q355B热轧钢板和钢带质量要求（模拟国标）",
        "variety": "热轧板",
        "grade": "Q355B",
        "spec": "厚度3.00mm-25.00mm，宽度1000mm-2200mm",
        "sample_spec": "厚度8.00mm，宽度1500mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 355, None, 345, None),
            ind("ind001", 470, 630, None, None),
            ind("ind002", 21, None, 20, None),
            ind("ind004", -0.250, 0.250, -0.300, 0.300),
        ],
    },
    {
        "seq": 4,
        "slug": "DX51D_Z_galvanized",
        "pdf": "GB-T-SIM-2518-2026_DX51D-Z_galvanized_sheet_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 2518-2026",
        "name": "DX51D+Z连续热镀锌钢板及钢带质量要求（模拟国标）",
        "variety": "镀锌板",
        "grade": "DX51D+Z",
        "spec": "厚度0.50mm-2.50mm，宽度800mm-1500mm",
        "sample_spec": "厚度1.00mm，宽度1250mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("td_ind_coat_mass", 80, 275, 70, 285),
            ind("ind001", 270, 500, None, None),
            ind("ind002", 22, None, 20, None),
            ind("ind004", -0.080, 0.080, -0.100, 0.100),
        ],
    },
    {
        "seq": 5,
        "slug": "06Cr19Ni10_stainless",
        "pdf": "GB-T-SIM-4237-2026_06Cr19Ni10_stainless_sheet_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 4237-2026",
        "name": "06Cr19Ni10不锈钢冷轧钢板质量要求（模拟国标）",
        "variety": "冷轧不锈钢板",
        "grade": "06Cr19Ni10",
        "spec": "厚度0.80mm-6.00mm，宽度800mm-1600mm",
        "sample_spec": "厚度2.00mm，宽度1219mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("td_ind_cr", 18.0, 20.0, 17.8, 20.2),
            ind("td_ind_ni", 8.0, 11.0, None, None),
            ind("ind001", 520, None, 510, None),
            ind("ind002", 40, None, 38, None),
        ],
    },
    {
        "seq": 6,
        "slug": "Q345R_pressure_vessel",
        "pdf": "GB-T-SIM-713-2026_Q345R_pressure_vessel_plate_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 713-2026",
        "name": "Q345R压力容器用钢板质量要求（模拟国标）",
        "variety": "压力容器钢板",
        "grade": "Q345R",
        "spec": "厚度6.00mm-40.00mm，宽度1500mm-2500mm",
        "sample_spec": "厚度16.00mm，宽度2000mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 345, None, 335, None),
            ind("ind001", 510, 640, None, None),
            ind("ind002", 21, None, 20, None),
            ind("td_ind_kv2", 34, None, 30, None),
        ],
    },
    {
        "seq": 7,
        "slug": "L245M_pipeline",
        "pdf": "GB-T-SIM-9711-2026_L245M_pipeline_steel_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 9711-2026",
        "name": "L245M管线钢板卷质量要求（模拟国标）",
        "variety": "管线钢板卷",
        "grade": "L245M",
        "spec": "厚度4.00mm-16.00mm，宽度1000mm-1800mm",
        "sample_spec": "厚度8.00mm，宽度1550mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 245, 450, 235, 460),
            ind("ind001", 415, 760, None, None),
            ind("ind002", 22, None, 20, None),
            ind("td_ind_ceq", None, 0.430, None, 0.450),
        ],
    },
    {
        "seq": 8,
        "slug": "DC04_deep_drawing",
        "pdf": "GB-T-SIM-5213-2026_DC04_deep_drawing_sheet_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 5213-2026",
        "name": "DC04深冲冷轧板质量要求（模拟国标）",
        "variety": "深冲冷轧板",
        "grade": "DC04",
        "spec": "厚度0.50mm-2.00mm，宽度800mm-1500mm",
        "sample_spec": "厚度0.80mm，宽度1250mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 140, 210, 130, 220),
            ind("ind001", 270, 350, None, None),
            ind("ind002", 38, None, 36, None),
            ind("ind008", 0.18, None, 0.16, None),
        ],
    },
    {
        "seq": 9,
        "slug": "50W800_electrical",
        "pdf": "GB-T-SIM-2521-2026_50W800_electrical_steel_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 2521-2026",
        "name": "50W800冷轧无取向电工钢质量要求（模拟国标）",
        "variety": "冷轧无取向电工钢",
        "grade": "50W800",
        "spec": "厚度0.47mm-0.53mm，宽度900mm-1250mm",
        "sample_spec": "厚度0.50mm，宽度1200mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("td_ind_p15_50", None, 8.00, None, 8.30),
            ind("td_ind_b50", 1.67, None, None, None),
            ind("ind004", -0.030, 0.030, -0.040, 0.040),
            ind("ind007", 130, 210, 120, 220),
        ],
    },
    {
        "seq": 10,
        "slug": "60Si2Mn_spring",
        "pdf": "GB-T-SIM-1222-2026_60Si2Mn_spring_flat_steel_national.pdf",
        "type": "NATIONAL",
        "code": "GB/T SIM 1222-2026",
        "name": "60Si2Mn热轧弹簧扁钢质量要求（模拟国标）",
        "variety": "热轧弹簧扁钢",
        "grade": "60Si2Mn",
        "spec": "厚度5.00mm-30.00mm，宽度40mm-160mm",
        "sample_spec": "厚度12.00mm，宽度80mm",
        "version": "2026版",
        "effective": "2026-04-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("td_ind_c", 0.56, 0.64, 0.55, 0.65),
            ind("td_ind_si", 1.50, 2.00, None, None),
            ind("td_ind_mn", 0.60, 0.90, 0.58, 0.92),
            ind("ind007", 300, 380, 290, 390),
        ],
    },
    {
        "seq": 11,
        "slug": "Q345B_enterprise",
        "pdf": "Q-JH-STEEL-SIM-2026-Q345B_hot_rolled_plate_enterprise.pdf",
        "type": "ENTERPRISE",
        "code": "Q/JH STEEL SIM 010-2026",
        "name": "Q345B低合金高强度结构钢板企业标准（模拟企标）",
        "variety": "热轧板",
        "grade": "Q345B",
        "spec": "厚度2.00mm-12.00mm，宽度900mm-1800mm",
        "sample_spec": "厚度6.00mm，宽度1500mm",
        "version": "V2026.1",
        "effective": "2026-03-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 345, None, 335, None),
            ind("ind001", 470, 630, None, None),
            ind("ind002", 21, None, 20, None),
            ind("ind004", -0.200, 0.200, -0.250, 0.250),
        ],
    },
    {
        "seq": 12,
        "slug": "SPFH590_pickled",
        "pdf": "Q-JH-STEEL-SIM-020-2026_SPFH590_pickled_strip_enterprise.pdf",
        "type": "ENTERPRISE",
        "code": "Q/JH STEEL SIM 020-2026",
        "name": "SPFH590酸洗热轧钢带企业标准（模拟企标）",
        "variety": "酸洗热轧钢带",
        "grade": "SPFH590",
        "spec": "厚度1.80mm-6.00mm，宽度800mm-1600mm",
        "sample_spec": "厚度3.00mm，宽度1250mm",
        "version": "V2026.1",
        "effective": "2026-03-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind001", 590, 760, 575, 780),
            ind("ind003", 420, None, None, None),
            ind("ind002", 18, None, 17, None),
            ind("ind004", -0.100, 0.100, -0.120, 0.120),
        ],
    },
    {
        "seq": 13,
        "slug": "HC340LA_enterprise",
        "pdf": "Q-JH-STEEL-SIM-030-2026_HC340LA_cold_rolled_enterprise.pdf",
        "type": "ENTERPRISE",
        "code": "Q/JH STEEL SIM 030-2026",
        "name": "HC340LA冷轧低合金高强钢板企业标准（模拟企标）",
        "variety": "冷轧低合金高强钢板",
        "grade": "HC340LA",
        "spec": "厚度0.80mm-2.50mm，宽度900mm-1500mm",
        "sample_spec": "厚度1.20mm，宽度1200mm",
        "version": "V2026.1",
        "effective": "2026-03-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 340, 430, 330, 440),
            ind("ind001", 410, 520, None, None),
            ind("ind002", 22, None, 20, None),
            ind("ind004", -0.060, 0.060, -0.080, 0.080),
        ],
    },
    {
        "seq": 14,
        "slug": "NM400_wear_plate",
        "pdf": "Q-JH-STEEL-SIM-040-2026_NM400_wear_plate_enterprise.pdf",
        "type": "ENTERPRISE",
        "code": "Q/JH STEEL SIM 040-2026",
        "name": "NM400调质耐磨钢板企业标准（模拟企标）",
        "variety": "调质耐磨钢板",
        "grade": "NM400",
        "spec": "厚度6.00mm-40.00mm，宽度1500mm-2500mm",
        "sample_spec": "厚度20.00mm，宽度2200mm",
        "version": "V2026.1",
        "effective": "2026-03-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind007", 360, 440, 350, 450),
            ind("ind001", 1200, None, None, None),
            ind("ind002", 8, None, 7, None),
            ind("ind004", -0.300, 0.300, -0.350, 0.350),
        ],
    },
    {
        "seq": 15,
        "slug": "S355J2_wind_tower",
        "pdf": "Q-JH-STEEL-SIM-050-2026_S355J2_wind_tower_plate_enterprise.pdf",
        "type": "ENTERPRISE",
        "code": "Q/JH STEEL SIM 050-2026",
        "name": "S355J2风电塔筒用中厚板企业标准（模拟企标）",
        "variety": "风电塔筒用中厚板",
        "grade": "S355J2",
        "spec": "厚度8.00mm-50.00mm，宽度1800mm-3200mm",
        "sample_spec": "厚度24.00mm，宽度2500mm",
        "version": "V2026.1",
        "effective": "2026-03-01",
        "expiry": "9999-12-31",
        "customer_id": None,
        "customer_name": None,
        "indicators": [
            ind("ind003", 355, None, 345, None),
            ind("ind001", 470, 630, None, None),
            ind("ind002", 22, None, 21, None),
            ind("td_ind_kv2", 27, None, 24, None),
        ],
    },
    {
        "seq": 16,
        "slug": "Q235B_customer_XNJC",
        "pdf": "CUST-XNJC-SIM-2026-Q235B_customer_agreement.pdf",
        "type": "CUSTOMER",
        "code": "AGREEMENT XNJC-SIM-2026-001",
        "name": "西南建材集团Q235B冷轧板供货质量协议（模拟客户协议）",
        "variety": "冷轧板",
        "grade": "Q235B",
        "spec": "厚度0.80mm-2.50mm，宽度800mm-1250mm",
        "sample_spec": "厚度1.20mm，宽度1000mm",
        "version": "协议V1.0",
        "effective": "2026-02-01",
        "expiry": "2026-12-31",
        "customer_id": "TD-CUST-XNJC",
        "customer_name": "西南建材集团",
        "indicators": [
            ind("ind001", 375, 505, 370, 510),
            ind("ind002", 27, None, None, None),
            ind("ind003", 235, 360, 225, 370),
            ind("ind004", -0.080, 0.080, -0.100, 0.100),
        ],
    },
    {
        "seq": 17,
        "slug": "HC340LA_customer_HDQC",
        "pdf": "CUST-HDQC-SIM-2026-HC340LA_customer_agreement.pdf",
        "type": "CUSTOMER",
        "code": "AGREEMENT HDQC-SIM-2026-002",
        "name": "华东汽车配件有限公司HC340LA冷轧结构板供货质量协议（模拟客户协议）",
        "variety": "冷轧低合金高强钢板",
        "grade": "HC340LA",
        "spec": "厚度0.90mm-2.00mm，宽度900mm-1450mm",
        "sample_spec": "厚度1.20mm，宽度1200mm",
        "version": "协议V1.0",
        "effective": "2026-03-01",
        "expiry": "2026-12-31",
        "customer_id": "TD-CUST-HDQC",
        "customer_name": "华东汽车配件有限公司",
        "indicators": [
            ind("ind003", 340, 410, 330, 420),
            ind("ind001", 420, 500, None, None),
            ind("ind002", 23, None, 22, None),
            ind("ind004", -0.050, 0.050, -0.060, 0.060),
        ],
    },
    {
        "seq": 18,
        "slug": "DX56D_Z_customer_BYEV",
        "pdf": "CUST-BYEV-SIM-2026-DX56D-Z_customer_agreement.pdf",
        "type": "CUSTOMER",
        "code": "AGREEMENT BYEV-SIM-2026-003",
        "name": "北源新能源DX56D+Z热镀锌深冲板供货质量协议（模拟客户协议）",
        "variety": "热镀锌深冲板",
        "grade": "DX56D+Z",
        "spec": "厚度0.60mm-1.60mm，宽度900mm-1450mm",
        "sample_spec": "厚度0.80mm，宽度1250mm",
        "version": "协议V1.0",
        "effective": "2026-03-01",
        "expiry": "2026-12-31",
        "customer_id": "TD-CUST-BYEV",
        "customer_name": "北源新能源",
        "indicators": [
            ind("ind002", 39, None, 37, None),
            ind("td_ind_coat_mass", 70, 140, None, None),
            ind("ind003", 120, 180, 110, 190),
            ind("ind004", -0.050, 0.050, -0.060, 0.060),
        ],
    },
    {
        "seq": 19,
        "slug": "AH36_customer_ZYSHIP",
        "pdf": "CUST-ZYSHIP-SIM-2026-AH36_customer_agreement.pdf",
        "type": "CUSTOMER",
        "code": "AGREEMENT ZYSHIP-SIM-2026-004",
        "name": "中远船务AH36船体结构钢板供货质量协议（模拟客户协议）",
        "variety": "船体结构钢板",
        "grade": "AH36",
        "spec": "厚度8.00mm-40.00mm，宽度1500mm-3000mm",
        "sample_spec": "厚度18.00mm，宽度2200mm",
        "version": "协议V1.0",
        "effective": "2026-03-01",
        "expiry": "2026-12-31",
        "customer_id": "TD-CUST-ZYSHIP",
        "customer_name": "中远船务",
        "indicators": [
            ind("ind003", 355, None, 345, None),
            ind("ind001", 490, 620, None, None),
            ind("ind002", 21, None, 20, None),
            ind("td_ind_kv2", 34, None, 30, None),
        ],
    },
    {
        "seq": 20,
        "slug": "L360M_customer_HXPIPE",
        "pdf": "CUST-HXPIPE-SIM-2026-L360M_customer_agreement.pdf",
        "type": "CUSTOMER",
        "code": "AGREEMENT HXPIPE-SIM-2026-005",
        "name": "华信管业L360M管线钢热轧卷供货质量协议（模拟客户协议）",
        "variety": "管线钢热轧卷",
        "grade": "L360M",
        "spec": "厚度5.00mm-18.00mm，宽度1000mm-1800mm",
        "sample_spec": "厚度10.00mm，宽度1500mm",
        "version": "协议V1.0",
        "effective": "2026-03-01",
        "expiry": "2026-12-31",
        "customer_id": "TD-CUST-HXPIPE",
        "customer_name": "华信管业",
        "indicators": [
            ind("ind003", 360, 510, 350, 520),
            ind("ind001", 460, 650, None, None),
            ind("ind002", 20, None, 19, None),
            ind("td_ind_ceq", None, 0.420, None, 0.440),
        ],
    },
]


def structured_grade(doc):
    return f"{doc['grade']}-QA0623"


def std_id(doc):
    return f"td_std_{doc['seq']:02d}_{doc['slug'].lower()}"[:63]


def sql_str(value):
    if value is None:
        return "NULL"
    return "'" + str(value).replace("'", "''") + "'"


def sql_num(value):
    if value is None:
        return "NULL"
    return format(value, "f")


def dec_str(value):
    if value is None:
        return "空"
    s = format(value, "f")
    if "." in s:
        s = s.rstrip("0").rstrip(".")
    return s


def indicator_name(indicator_id):
    return BASE_INDICATORS[indicator_id][0]


def indicator_unit(indicator_id):
    return BASE_INDICATORS[indicator_id][3]


def normal_value(item):
    lower, upper = item["lower"], item["upper"]
    if lower is not None and upper is not None:
        return (lower + upper) / Decimal("2")
    if lower is not None:
        if lower.copy_abs() >= Decimal("100"):
            return lower + Decimal("25")
        if lower.copy_abs() >= Decimal("10"):
            return lower + Decimal("2")
        if lower.copy_abs() >= Decimal("1"):
            return lower + Decimal("0.2")
        return lower + Decimal("0.02")
    if upper is not None:
        if upper.copy_abs() >= Decimal("100"):
            return upper - Decimal("25")
        if upper.copy_abs() >= Decimal("10"):
            return upper - Decimal("2")
        if upper.copy_abs() >= Decimal("1"):
            return upper - Decimal("0.2")
        return upper - Decimal("0.02")
    return Decimal("0")


def edge_value(item):
    if item["lower"] is not None:
        return item["lower"]
    if item["upper"] is not None:
        return item["upper"]
    return normal_value(item)


def concession_value(item):
    lower, upper, cl, cu = item["lower"], item["upper"], item["cl"], item["cu"]
    if lower is not None and cl is not None:
        return (lower + cl) / Decimal("2")
    if upper is not None and cu is not None:
        return (upper + cu) / Decimal("2")
    return normal_value(item)


def unqualified_value(item):
    lower, upper, cl, cu = item["lower"], item["upper"], item["cl"], item["cu"]
    if lower is not None and cl is not None:
        return cl - (lower - cl).copy_abs() - Decimal("0.01")
    if upper is not None and cu is not None:
        return cu + (cu - upper).copy_abs() + Decimal("0.01")
    if lower is not None:
        return lower - Decimal("10")
    if upper is not None:
        return upper + Decimal("10")
    return normal_value(item)


def reinspection_value(item):
    lower, upper = item["lower"], item["upper"]
    if lower is not None:
        delta = Decimal("5") if lower.copy_abs() >= Decimal("100") else Decimal("0.5")
        return lower - delta
    if upper is not None:
        delta = Decimal("5") if upper.copy_abs() >= Decimal("100") else Decimal("0.05")
        return upper + delta
    return normal_value(item)


def record_values(doc, scenario):
    values = [normal_value(item) for item in doc["indicators"]]
    if scenario == "QUALIFIED_EDGE":
        values = [edge_value(item) for item in doc["indicators"]]
    elif scenario == "CAN_CONCESSION":
        values[0] = concession_value(doc["indicators"][0])
    elif scenario == "NEED_REINSPECTION":
        values[1] = reinspection_value(doc["indicators"][1])
    elif scenario == "UNQUALIFIED":
        values[0] = unqualified_value(doc["indicators"][0])
    return values


SCENARIOS = [
    ("QUAL", "QUALIFIED", "全部关键指标处于合格范围内"),
    ("EDGE", "QUALIFIED", "关键指标取边界值，验证边界包含"),
    ("CONC", "CAN_CONCESSION", "首个关键指标轻微超出合格范围但位于让步范围内"),
    ("REINS", "NEED_REINSPECTION", "第二个关键指标超限且该方向未配置让步范围"),
    ("UNQ", "UNQUALIFIED", "首个关键指标超出让步范围"),
]


def record_id(doc, index):
    return f"td_ins_{doc['seq']:02d}_{index}"


def heat_no(doc, tag):
    return f"HT-TD-{doc['seq']:02d}-{tag}-001"


def coil_no(doc, tag):
    return f"COIL-TD-{doc['seq']:02d}-{tag}-001"


def standard_code(doc):
    return f"{doc['code']}-QA0623"


def standard_name(doc):
    return doc["name"].replace("（模拟", "（QA0623隔离模拟")


def make_indicator_insert(indicator_ids):
    rows = []
    for iid in sorted(indicator_ids):
        name, code, cat, unit, method, desc = BASE_INDICATORS[iid]
        rows.append(
            f"({sql_str(iid)}, {sql_str(name)}, {sql_str(code)}, {sql_str(cat)}, {sql_str(unit)}, "
            f"{sql_str(method)}, {sql_str(desc)}, 'ACTIVE', 0, NOW(), NOW())"
        )
    return (
        "INSERT IGNORE INTO qc_indicator_item\n"
        "(id, indicator_name, indicator_code, indicator_category, unit, test_method, description, status, is_deleted, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(rows) + ";\n"
    )


def make_dict_insert(doc):
    rows = [
        (
            f"td_var_{doc['seq']:02d}",
            "PRODUCT_VARIETY",
            doc["variety"],
            doc["variety"],
            "测试包：100份检验记录样例",
        ),
        (
            f"td_grade_{doc['seq']:02d}",
            "PRODUCT_GRADE",
            structured_grade(doc),
            structured_grade(doc),
            "测试包：结构化牌号使用QA0623后缀，避免历史脏数据",
        ),
    ]
    if doc["customer_id"]:
        rows.append(
            (
                f"td_cust_{doc['seq']:02d}",
                "QC_CUSTOMER",
                doc["customer_id"],
                doc["customer_name"],
                "测试包：客户协议检验记录样例",
            )
        )
    values = []
    for idx, (rid, dcode, val, label, remark) in enumerate(rows, start=1):
        values.append(
            f"({sql_str(rid)}, {sql_str(dcode)}, {sql_str(val)}, {sql_str(label)}, '', {200 + doc['seq'] * 10 + idx}, 1, 0, {sql_str(remark)}, NOW(), NOW())"
        )
    return (
        "INSERT IGNORE INTO sys_dict_item\n"
        "(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(values) + ";\n"
    )


def make_conflict_dict_insert(cfg):
    rows = [
        (
            f"td_cf_var_{cfg['seq']:02d}",
            "PRODUCT_VARIETY",
            cfg["variety"],
            cfg["variety"],
            "测试包：冲突标准样例",
        ),
        (
            f"td_cf_grade_{cfg['seq']:02d}",
            "PRODUCT_GRADE",
            structured_grade(cfg),
            structured_grade(cfg),
            "测试包：冲突标准结构化牌号使用QA0623后缀",
        ),
        (
            f"td_cf_cust_{cfg['seq']:02d}",
            "QC_CUSTOMER",
            cfg["customer_id"],
            cfg["customer_name"],
            "测试包：冲突标准独立客户，避免影响普通检验记录",
        ),
    ]
    values = []
    for idx, (rid, dcode, val, label, remark) in enumerate(rows, start=1):
        values.append(
            f"({sql_str(rid)}, {sql_str(dcode)}, {sql_str(val)}, {sql_str(label)}, '', {700 + cfg['seq'] * 10 + idx}, 1, 0, {sql_str(remark)}, NOW(), NOW())"
        )
    return (
        "INSERT IGNORE INTO sys_dict_item\n"
        "(id, dict_code, item_value, item_label, color_tag, sort_no, status, is_system, remark, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(values) + ";\n"
    )


def make_standard_sql(doc):
    sid = std_id(doc)
    rows = [
        f"({sql_str(sid)}, {sql_str(doc['type'])}, {sql_str(standard_code(doc))}, {sql_str(standard_name(doc))}, "
        f"{sql_str(doc['variety'])}, {sql_str(structured_grade(doc))}, {sql_str(doc['spec'])}, "
        f"{sql_str(doc['version'] + '-QA0623')}, {sql_str(doc['effective'])}, {sql_str(doc['expiry'])}, 'PUBLISHED', "
        f"{sql_str(doc['customer_id'])}, {sql_str('来源PDF：' + doc['pdf'] + '；结构化牌号使用QA0623后缀以避让历史脏数据')}, "
        "'system', NOW(), NOW())"
    ]
    sql = (
        "INSERT IGNORE INTO qc_quality_standard\n"
        "(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(rows) + ";\n"
    )
    si_rows = []
    for idx, item in enumerate(doc["indicators"], start=1):
        si_id = f"td_si_{doc['seq']:02d}_{idx}"
        si_rows.append(
            f"({sql_str(si_id)}, {sql_str(sid)}, {sql_str(item['id'])}, "
            f"{sql_num(item['upper'])}, {sql_num(item['lower'])}, 1, {sql_num(item['cu'])}, {sql_num(item['cl'])}, NOW(), NOW())"
        )
    sql += (
        "\nINSERT IGNORE INTO qc_standard_indicator\n"
        "(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(si_rows) + ";\n"
    )
    return sql


def make_records_sql(doc):
    rec_rows = []
    val_rows = []
    for idx, (tag, expected, _) in enumerate(SCENARIOS, start=1):
        rid = record_id(doc, idx)
        test_time = f"2026-06-{23 + (doc['seq'] - 1) // 4:02d} {8 + idx:02d}:{(doc['seq'] * 3) % 60:02d}:00"
        rec_rows.append(
            f"({sql_str(rid)}, {sql_str(heat_no(doc, tag))}, {sql_str(coil_no(doc, tag))}, {sql_str(heat_no(doc, tag))}, "
            f"{sql_str(['MIDDLE', 'HEAD', 'TAIL', 'MIDDLE', 'HEAD'][idx - 1])}, {sql_str(test_time)}, '021001', "
            f"{sql_str(doc['customer_id'])}, {sql_str(doc['variety'])}, {sql_str(structured_grade(doc))}, "
            f"{sql_str(doc['sample_spec'])}, 'NORMAL', 'system', NOW(), NOW())"
        )
        values = record_values(doc, expected if tag != "EDGE" else "QUALIFIED_EDGE")
        for value_idx, (item, value) in enumerate(zip(doc["indicators"], values), start=1):
            value_id = f"td_iv_{doc['seq']:02d}_{idx}_{value_idx}"
            val_rows.append(
                f"({sql_str(value_id)}, {sql_str(rid)}, {sql_str(item['id'])}, "
                f"{sql_num(value)}, NULL, 'system', NOW(), NOW())"
            )
    return (
        "INSERT IGNORE INTO qc_inspection_record\n"
        "(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(rec_rows) + ";\n\n"
        "INSERT IGNORE INTO qc_inspection_value\n"
        "(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(val_rows) + ";\n"
    )


def make_doc_sql(doc):
    indicator_ids = {item["id"] for item in doc["indicators"]}
    return (
        "USE ai_quality_control;\n\n"
        f"-- ============================================================\n"
        f"-- Inspect sample {doc['seq']:03d}: {doc['slug']}\n"
        f"-- Source PDF: {STD_DOC_DIR / doc['pdf']}\n"
        f"-- Rerunnable: INSERT IGNORE only.\n"
        f"-- Direct SQL insertion does not invoke InspectionService.addRecord().\n"
        f"-- Use the API payloads in the paired MD file when automatic judgment must be generated by service logic.\n"
        f"-- ============================================================\n\n"
        + make_dict_insert(doc)
        + "\n"
        + make_indicator_insert(indicator_ids)
        + "\n"
        + make_standard_sql(doc)
        + "\n"
        + make_records_sql(doc)
    )


def make_api_payload(doc, idx, tag):
    expected = SCENARIOS[idx - 1][1]
    if tag == "EDGE":
        values = record_values(doc, "QUALIFIED_EDGE")
    else:
        values = record_values(doc, expected)
    return {
        "heatNo": heat_no(doc, tag),
        "coilNo": coil_no(doc, tag),
        "customerId": doc["customer_id"],
        "productVariety": doc["variety"],
        "productGrade": structured_grade(doc),
        "productSpec": doc["sample_spec"],
        "sampleType": ["MIDDLE", "HEAD", "TAIL", "MIDDLE", "HEAD"][idx - 1],
        "testTime": f"2026-06-{23 + (doc['seq'] - 1) // 4:02d} {8 + idx:02d}:{(doc['seq'] * 3) % 60:02d}:00",
        "testerNo": "021001",
        "values": [
            {"indicatorId": item["id"], "testValue": float(values[i])}
            for i, item in enumerate(doc["indicators"])
        ],
    }


def make_doc_md(doc):
    lines = []
    lines.append(f"# 检验记录样例 {doc['seq']:03d} - {doc['grade']} / {doc['variety']}")
    lines.append("")
    lines.append("## 1. 样例定位")
    lines.append("")
    lines.append("| 项目 | 内容 |")
    lines.append("| --- | --- |")
    lines.append(f"| 样例编号 | `INSPECT-SAMPLE-{doc['seq']:03d}` |")
    lines.append(f"| 来源PDF | `{STD_DOC_DIR / doc['pdf']}` |")
    lines.append(f"| PDF标准编号 | `{doc['code']}` |")
    lines.append(f"| SQL脚本 | `inspect_sample_{doc['seq']:03d}_{doc['slug']}.sql` |")
    lines.append(f"| 标准类型 | `{doc['type']}` |")
    lines.append(f"| 结构化牌号 | `{structured_grade(doc)}` |")
    lines.append("| 记录数量 | `5` |")
    lines.append("")
    lines.append("## 2. 标准录入字段")
    lines.append("")
    lines.append("| 字段 | 值 |")
    lines.append("| --- | --- |")
    lines.append(f"| 标准ID | `{std_id(doc)}` |")
    lines.append(f"| 标准编号 | `{standard_code(doc)}` |")
    lines.append(f"| 标准名称 | `{standard_name(doc)}` |")
    lines.append(f"| 品种 | `{doc['variety']}` |")
    lines.append(f"| 牌号 | `{structured_grade(doc)}` |")
    lines.append(f"| 规格范围 | `{doc['spec']}` |")
    lines.append(f"| 客户 | `{doc['customer_id'] or '空'}` |")
    lines.append(f"| 生效日期 | `{doc['effective']}` |")
    lines.append(f"| 失效日期 | `{doc['expiry']}` |")
    lines.append("")
    lines.append("## 3. 结构化指标")
    lines.append("")
    lines.append("| 指标ID | 指标 | 合格下限 | 合格上限 | 让步下限 | 让步上限 | 单位 |")
    lines.append("| --- | --- | --- | --- | --- | --- | --- |")
    for item in doc["indicators"]:
        lines.append(
            f"| `{item['id']}` | {indicator_name(item['id'])} | {dec_str(item['lower'])} | {dec_str(item['upper'])} | "
            f"{dec_str(item['cl'])} | {dec_str(item['cu'])} | {indicator_unit(item['id'])} |"
        )
    lines.append("")
    lines.append("## 4. 五条检验记录")
    lines.append("")
    lines.append("| 序号 | 场景 | 记录ID | 炉号 | 卷号 | 预期判定 |")
    lines.append("| --- | --- | --- | --- | --- | --- |")
    for idx, (tag, expected, desc) in enumerate(SCENARIOS, start=1):
        lines.append(
            f"| {idx} | {desc} | `{record_id(doc, idx)}` | `{heat_no(doc, tag)}` | `{coil_no(doc, tag)}` | `{expected}` |"
        )
    lines.append("")
    lines.append("检验值明细：")
    lines.append("")
    header = "| 记录ID | " + " | ".join([indicator_name(item["id"]) for item in doc["indicators"]]) + " |"
    lines.append(header)
    lines.append("| --- | " + " | ".join(["---"] * len(doc["indicators"])) + " |")
    for idx, (tag, expected, _) in enumerate(SCENARIOS, start=1):
        scenario_key = "QUALIFIED_EDGE" if tag == "EDGE" else expected
        values = record_values(doc, scenario_key)
        lines.append("| `" + record_id(doc, idx) + "` | " + " | ".join(dec_str(v) for v in values) + " |")
    lines.append("")
    lines.append("## 5. 使用说明")
    lines.append("")
    lines.append("1. 直接执行本样例SQL可插入标准、指标、检验记录和检验值。")
    lines.append("2. 直接SQL插入不会调用后端 `InspectionService.addRecord()`，因此不会自动生成 `qc_judgment_result`。")
    lines.append("3. 如需验证规则引擎自动判定，请先执行本SQL中的标准和字典数据，随后按下方API payload通过页面或接口新增检验记录。")
    lines.append("4. 结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅代表来源材料。")
    lines.append("")
    lines.append("## 6. API Payloads")
    for idx, (tag, _, _) in enumerate(SCENARIOS, start=1):
        lines.append("")
        lines.append(f"### {record_id(doc, idx)}")
        lines.append("")
        lines.append("```json")
        lines.append(json.dumps(make_api_payload(doc, idx, tag), ensure_ascii=False, indent=2))
        lines.append("```")
    lines.append("")
    return "\n".join(lines)


def conflict_configs():
    # Use conflict-only customers to keep these records from affecting the 100 normal inspection samples.
    source_docs = [DOCS[16], DOCS[15], DOCS[17], DOCS[18], DOCS[19]]
    names = [
        ("HC340LA_customer", "TD-CUST-HDQC-CF", "华东汽车配件有限公司冲突测试客户"),
        ("Q235B_customer", "TD-CUST-XNJC-CF", "西南建材集团冲突测试客户"),
        ("DX56D_Z_customer", "TD-CUST-BYEV-CF", "北源新能源冲突测试客户"),
        ("AH36_customer", "TD-CUST-ZYSHIP-CF", "中远船务冲突测试客户"),
        ("L360M_customer", "TD-CUST-HXPIPE-CF", "华信管业冲突测试客户"),
    ]
    configs = []
    for seq, (doc, (slug, cust_id, cust_name)) in enumerate(zip(source_docs, names), start=1):
        base = dict(doc)
        base["seq"] = seq
        base["slug"] = slug
        base["customer_id"] = cust_id
        base["customer_name"] = cust_name
        configs.append(base)
    return configs


def tighten(item):
    item = dict(item)
    lower, upper = item["lower"], item["upper"]
    if lower is not None:
        if lower.copy_abs() >= Decimal("100"):
            item["lower"] = lower + Decimal("10")
        elif lower.copy_abs() >= Decimal("10"):
            item["lower"] = lower + Decimal("1")
        elif lower.copy_abs() >= Decimal("1"):
            item["lower"] = lower + Decimal("0.1")
        else:
            item["lower"] = lower + Decimal("0.01")
    if upper is not None:
        if upper.copy_abs() >= Decimal("100"):
            item["upper"] = upper - Decimal("5")
        elif upper.copy_abs() >= Decimal("10"):
            item["upper"] = upper - Decimal("0.5")
        elif upper.copy_abs() >= Decimal("1"):
            item["upper"] = upper - Decimal("0.05")
        else:
            item["upper"] = upper - Decimal("0.005")
    item["cl"] = None
    item["cu"] = None
    return item


def conflict_ids(cfg):
    return (
        f"td_conf_{cfg['seq']:02d}_v1",
        f"td_conf_{cfg['seq']:02d}_v2",
        f"td_conf_ins_{cfg['seq']:02d}",
        f"td_conf_jud_{cfg['seq']:02d}",
        f"td_scf_{cfg['seq']:02d}",
    )


def conflict_code(cfg, rev=False):
    suffix = "REV-CF-QA0623" if rev else "QA0623"
    return f"{cfg['code']}-{suffix}"


def conflict_values(cfg, v2_indicators):
    vals = [normal_value(item) for item in cfg["indicators"]]
    # Make the first value pass V1 and fail V2.
    item = cfg["indicators"][0]
    v2 = v2_indicators[0]
    if item["lower"] is not None and v2["lower"] is not None:
        vals[0] = (item["lower"] + v2["lower"]) / Decimal("2")
    elif item["upper"] is not None and v2["upper"] is not None:
        vals[0] = (item["upper"] + v2["upper"]) / Decimal("2")
    return vals


def make_conflict_sql(cfg):
    v1_id, v2_id, rec_id, jud_id, conflict_id = conflict_ids(cfg)
    v2_indicators = [tighten(item) for item in cfg["indicators"]]
    values = conflict_values(cfg, v2_indicators)
    all_indicator_ids = {item["id"] for item in cfg["indicators"]}
    dict_sql = make_conflict_dict_insert(cfg)
    ind_sql = make_indicator_insert(all_indicator_ids)
    std_rows = []
    for sid, code, version, indicators, remark in [
        (v1_id, conflict_code(cfg, False), "协议V1.0-CF-QA0623", cfg["indicators"], "冲突样例V1：客户协议基准版"),
        (v2_id, conflict_code(cfg, True), "协议V1.1-CF-QA0623", v2_indicators, "冲突样例V2：同优先级同范围限值收严"),
    ]:
        std_rows.append(
            f"({sql_str(sid)}, 'CUSTOMER', {sql_str(code)}, {sql_str(cfg['name'] + ('冲突版' if sid == v2_id else '基准版'))}, "
            f"{sql_str(cfg['variety'])}, {sql_str(structured_grade(cfg))}, {sql_str(cfg['spec'])}, {sql_str(version)}, "
            f"{sql_str('2026-03-01' if sid == v1_id else '2026-04-15')}, '2026-12-31', 'PUBLISHED', {sql_str(cfg['customer_id'])}, "
            f"{sql_str(remark + '；来源PDF：' + cfg['pdf'])}, 'system', NOW(), NOW())"
        )
    si_rows = []
    for std_index, (sid, indicators) in enumerate([(v1_id, cfg["indicators"]), (v2_id, v2_indicators)], start=1):
        for idx, item in enumerate(indicators, start=1):
            csi_id = f"td_csi_{cfg['seq']:02d}_{std_index}_{idx}"
            si_rows.append(
                f"({sql_str(csi_id)}, {sql_str(sid)}, {sql_str(item['id'])}, "
                f"{sql_num(item['upper'])}, {sql_num(item['lower'])}, 1, {sql_num(item['cu'])}, {sql_num(item['cl'])}, NOW(), NOW())"
            )
    val_rows = []
    for idx, (item, value) in enumerate(zip(cfg["indicators"], values), start=1):
        civ_id = f"td_civ_{cfg['seq']:02d}_{idx}"
        val_rows.append(
            f"({sql_str(civ_id)}, {sql_str(rec_id)}, {sql_str(item['id'])}, {sql_num(value)}, NULL, 'system', NOW(), NOW())"
        )
    conflict_detail = {
        "reason": "同一客户、同一品种牌号、同一规格窗口内存在两份已发布客户协议，且有效期重叠；至少一个关键指标结构化限值不一致。",
        "standards": [
            {"standardId": v1_id, "standardCode": conflict_code(cfg, False), "indicator": cfg["indicators"][0]["id"], "lowerLimit": float(cfg["indicators"][0]["lower"]) if cfg["indicators"][0]["lower"] is not None else None, "upperLimit": float(cfg["indicators"][0]["upper"]) if cfg["indicators"][0]["upper"] is not None else None},
            {"standardId": v2_id, "standardCode": conflict_code(cfg, True), "indicator": v2_indicators[0]["id"], "lowerLimit": float(v2_indicators[0]["lower"]) if v2_indicators[0]["lower"] is not None else None, "upperLimit": float(v2_indicators[0]["upper"]) if v2_indicators[0]["upper"] is not None else None},
        ],
        "blocking": True,
    }
    first = cfg["indicators"][0]
    cf_heat_no = f"HT-TD-CF-{cfg['seq']:02d}-001"
    cf_coil_no = f"COIL-TD-CF-{cfg['seq']:02d}-001"
    cf_no = f"TD-SCF-{cfg['seq']:03d}"
    return (
        "USE ai_quality_control;\n\n"
        f"-- ============================================================\n"
        f"-- Conflict sample {cfg['seq']:03d}: {cfg['slug']}\n"
        f"-- Source PDF: {STD_DOC_DIR / cfg['pdf']}\n"
        f"-- Rerunnable: INSERT IGNORE only.\n"
        f"-- ============================================================\n\n"
        + dict_sql
        + "\n"
        + ind_sql
        + "\nINSERT IGNORE INTO qc_quality_standard\n"
        "(id, standard_type, standard_code, standard_name, variety, grade, spec_range, version_no, effective_date, expiry_date, status, customer_id, remark, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(std_rows) + ";\n\n"
        + "INSERT IGNORE INTO qc_standard_indicator\n"
        "(id, standard_id, indicator_id, upper_limit, lower_limit, is_required, concession_upper, concession_lower, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(si_rows) + ";\n\n"
        + "INSERT IGNORE INTO qc_inspection_record\n"
        "(id, heat_no, coil_no, batch_no, sample_type, test_time, tester_no, customer_id, product_variety, product_grade, product_spec, status, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n"
        f"({sql_str(rec_id)}, {sql_str(cf_heat_no)}, {sql_str(cf_coil_no)}, {sql_str(cf_heat_no)}, "
        f"'MIDDLE', '2026-06-28 10:{cfg['seq'] * 7:02d}:00', '021001', {sql_str(cfg['customer_id'])}, {sql_str(cfg['variety'])}, {sql_str(structured_grade(cfg))}, "
        f"{sql_str(cfg['sample_spec'])}, 'NORMAL', 'system', NOW(), NOW());\n\n"
        + "INSERT IGNORE INTO qc_inspection_value\n"
        "(id, record_id, indicator_id, test_value, value_text, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n" + ",\n".join(val_rows) + ";\n\n"
        + "INSERT IGNORE INTO qc_judgment_result\n"
        "(id, record_id, judgment_type, judgment_time, is_final, matched_standard_ids, remark, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n"
        f"({sql_str(jud_id)}, {sql_str(rec_id)}, 'STANDARD_CONFLICT', '2026-06-28 10:{cfg['seq'] * 7:02d}:05', 1, "
        f"{sql_str(json.dumps([v1_id, v2_id], ensure_ascii=False))}, '测试包冲突样例：同优先级客户协议结构化限值冲突，需人工裁决', 'system', NOW(), NOW());\n\n"
        + "INSERT IGNORE INTO standard_conflict\n"
        "(id, conflict_no, judgment_id, record_id, conflict_type, conflict_level, status, indicator_id, indicator_name, unit, customer_id, variety, grade, product_spec, inspection_date, selected_standard_id, involved_standard_ids, conflict_detail, selected_priority, create_user_no, create_date_time, update_date_time)\n"
        "VALUES\n"
        f"({sql_str(conflict_id)}, {sql_str(cf_no)}, {sql_str(jud_id)}, {sql_str(rec_id)}, 'NUMERIC_LIMIT', 'BLOCKING', 'PENDING', "
        f"{sql_str(first['id'])}, {sql_str(indicator_name(first['id']))}, {sql_str(indicator_unit(first['id']))}, {sql_str(cfg['customer_id'])}, "
        f"{sql_str(cfg['variety'])}, {sql_str(structured_grade(cfg))}, {sql_str(cfg['sample_spec'])}, '2026-06-28', NULL, "
        f"{sql_str(json.dumps([v1_id, v2_id], ensure_ascii=False))}, {sql_str(json.dumps(conflict_detail, ensure_ascii=False))}, 'CUSTOMER', 'system', NOW(), NOW());\n"
    )


def make_conflict_md(cfg):
    v1_id, v2_id, rec_id, jud_id, conflict_id = conflict_ids(cfg)
    v2_indicators = [tighten(item) for item in cfg["indicators"]]
    values = conflict_values(cfg, v2_indicators)
    lines = []
    lines.append(f"# 冲突标准样例 {cfg['seq']:03d} - {cfg['grade']} / {cfg['variety']}")
    lines.append("")
    lines.append("| 项目 | 内容 |")
    lines.append("| --- | --- |")
    lines.append(f"| 样例编号 | `CONFLICT-SAMPLE-{cfg['seq']:03d}` |")
    lines.append(f"| 来源PDF | `{STD_DOC_DIR / cfg['pdf']}` |")
    lines.append(f"| SQL脚本 | `conflict_sample_{cfg['seq']:03d}_{cfg['slug']}.sql` |")
    lines.append(f"| 客户 | `{cfg['customer_id']} / {cfg['customer_name']}` |")
    lines.append(f"| 品种 | `{cfg['variety']}` |")
    lines.append(f"| 牌号 | `{structured_grade(cfg)}` |")
    lines.append(f"| 冲突编号 | `TD-SCF-{cfg['seq']:03d}` |")
    lines.append("| 预期 | `STANDARD_CONFLICT / BLOCKING / PENDING` |")
    lines.append("")
    lines.append("## 1. 冲突标准")
    lines.append("")
    lines.append("| 字段 | V1基准协议 | V2冲突协议 |")
    lines.append("| --- | --- | --- |")
    lines.append(f"| 标准ID | `{v1_id}` | `{v2_id}` |")
    lines.append(f"| 标准编号 | `{conflict_code(cfg, False)}` | `{conflict_code(cfg, True)}` |")
    lines.append(f"| 有效期 | `2026-03-01 至 2026-12-31` | `2026-04-15 至 2026-12-31` |")
    lines.append(f"| 规格范围 | `{cfg['spec']}` | `{cfg['spec']}` |")
    lines.append("")
    lines.append("## 2. 限值差异")
    lines.append("")
    lines.append("| 指标 | V1下限 | V1上限 | V2下限 | V2上限 | 单位 |")
    lines.append("| --- | --- | --- | --- | --- | --- |")
    for left, right in zip(cfg["indicators"], v2_indicators):
        lines.append(
            f"| {indicator_name(left['id'])} | {dec_str(left['lower'])} | {dec_str(left['upper'])} | "
            f"{dec_str(right['lower'])} | {dec_str(right['upper'])} | {indicator_unit(left['id'])} |"
        )
    lines.append("")
    lines.append("## 3. 触发检验记录")
    lines.append("")
    lines.append(f"| 记录ID | `{rec_id}` |")
    lines.append("| --- | --- |")
    lines.append(f"| 炉号 | `HT-TD-CF-{cfg['seq']:02d}-001` |")
    lines.append(f"| 卷号 | `COIL-TD-CF-{cfg['seq']:02d}-001` |")
    lines.append(f"| 客户 | `{cfg['customer_id']}` |")
    lines.append(f"| 品种/牌号 | `{cfg['variety']} / {structured_grade(cfg)}` |")
    lines.append(f"| 规格 | `{cfg['sample_spec']}` |")
    lines.append("")
    lines.append("| 指标 | 实测值 | 单位 |")
    lines.append("| --- | --- | --- |")
    for item, value in zip(cfg["indicators"], values):
        lines.append(f"| {indicator_name(item['id'])} | {dec_str(value)} | {indicator_unit(item['id'])} |")
    lines.append("")
    lines.append("## 4. 验证点")
    lines.append("")
    lines.append("1. 标准冲突列表出现对应 `TD-SCF-*` 编号。")
    lines.append("2. 冲突等级为 `BLOCKING`，状态为 `PENDING`。")
    lines.append("3. 涉及标准包含V1和V2两份客户协议。")
    lines.append("4. 正式质保书或最终放行流程应被阻断，直到人工裁决控制标准。")
    lines.append("5. 本样例使用 `TD-CUST-*-CF` 客户和 `QA0623` 牌号后缀，避免影响普通100条检验记录。")
    lines.append("")
    return "\n".join(lines)


def write_all():
    inspect_index = ["# 100份检验记录样例索引", "", "结构化牌号统一使用 `QA0623` 后缀，PDF正文中的原牌号仅作为来源材料。", ""]
    inspect_index.append("| 序号 | PDF | SQL | 品种 | 结构化牌号 | 客户 | 记录数 |")
    inspect_index.append("| --- | --- | --- | --- | --- | --- | --- |")
    all_inspect_sql_parts = ["USE ai_quality_control;\n"]
    for doc in DOCS:
        md_name = f"inspect_sample_{doc['seq']:03d}_{doc['slug']}.md"
        sql_name = f"inspect_sample_{doc['seq']:03d}_{doc['slug']}.sql"
        (INSPECT_DIR / md_name).write_text(make_doc_md(doc), encoding="utf-8")
        doc_sql = make_doc_sql(doc)
        (INSPECT_DIR / sql_name).write_text(doc_sql, encoding="utf-8")
        all_inspect_sql_parts.append(f"\n-- ===== {sql_name} =====\n" + doc_sql.replace("USE ai_quality_control;\n\n", ""))
        inspect_index.append(
            f"| {doc['seq']:03d} | `{doc['pdf']}` | `{sql_name}` | {doc['variety']} | `{structured_grade(doc)}` | `{doc['customer_id'] or '空'}` | 5 |"
        )
    inspect_index.append("")
    inspect_index.append("## 判定分布")
    inspect_index.append("")
    inspect_index.append("每份PDF包含：2条 `QUALIFIED`、1条 `CAN_CONCESSION`、1条 `NEED_REINSPECTION`、1条 `UNQUALIFIED`。")
    inspect_index.append("合计100条检验记录。直接执行SQL不会自动生成判定结果；如需验证自动判定，请使用各MD中的API payload录入。")
    inspect_index.append("")
    (INSPECT_DIR / "README.md").write_text("\n".join(inspect_index), encoding="utf-8")
    (INSPECT_DIR / "all_inspection_samples.sql").write_text("\n".join(all_inspect_sql_parts), encoding="utf-8")

    conflict_index = ["# 5个冲突标准样例索引", "", "冲突样例使用独立 `TD-CUST-*-CF` 客户，避免影响普通检验记录样例。", ""]
    conflict_index.append("| 序号 | PDF | SQL | 品种 | 结构化牌号 | 冲突编号 |")
    conflict_index.append("| --- | --- | --- | --- | --- | --- |")
    all_conflict_sql_parts = ["USE ai_quality_control;\n"]
    for cfg in conflict_configs():
        md_name = f"conflict_sample_{cfg['seq']:03d}_{cfg['slug']}.md"
        sql_name = f"conflict_sample_{cfg['seq']:03d}_{cfg['slug']}.sql"
        (CONFLICT_DIR / md_name).write_text(make_conflict_md(cfg), encoding="utf-8")
        cfg_sql = make_conflict_sql(cfg)
        (CONFLICT_DIR / sql_name).write_text(cfg_sql, encoding="utf-8")
        all_conflict_sql_parts.append(f"\n-- ===== {sql_name} =====\n" + cfg_sql.replace("USE ai_quality_control;\n\n", ""))
        conflict_index.append(
            f"| {cfg['seq']:03d} | `{cfg['pdf']}` | `{sql_name}` | {cfg['variety']} | `{structured_grade(cfg)}` | `TD-SCF-{cfg['seq']:03d}` |"
        )
    conflict_index.append("")
    (CONFLICT_DIR / "README.md").write_text("\n".join(conflict_index), encoding="utf-8")
    (CONFLICT_DIR / "all_conflict_samples.sql").write_text("\n".join(all_conflict_sql_parts), encoding="utf-8")


if __name__ == "__main__":
    write_all()
